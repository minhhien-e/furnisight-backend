package com.furnisight.message.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageSchemaGuard {
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureAttachmentSchema() {
        ensureConversationUniqueness();

        jdbcTemplate.execute("""
            ALTER TABLE messages
            ADD COLUMN IF NOT EXISTS attachments TEXT
            """);

        jdbcTemplate.execute("""
            DO $$
            BEGIN
                IF EXISTS (
                    SELECT 1
                    FROM pg_constraint
                    WHERE conname = 'messages_type_check'
                      AND conrelid = 'messages'::regclass
                ) THEN
                    ALTER TABLE messages DROP CONSTRAINT messages_type_check;
                END IF;

                ALTER TABLE messages
                ADD CONSTRAINT messages_type_check
                CHECK (type IN ('TEXT', 'IMAGE', 'FILE', 'ORDER_LINK', 'PRODUCT_LINK'));
            END $$;
            """);
    }

    private void ensureConversationUniqueness() {
        jdbcTemplate.execute("""
            UPDATE conversations
            SET channel = 'SUPPORT'
            WHERE channel IS NULL
            """);

        jdbcTemplate.execute("""
            WITH ranked AS (
                SELECT
                    id,
                    FIRST_VALUE(id) OVER (
                        PARTITION BY buyerid
                        ORDER BY updatedat DESC NULLS LAST, id DESC
                    ) AS keep_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY buyerid
                        ORDER BY updatedat DESC NULLS LAST, id DESC
                    ) AS rn
                FROM conversations
                WHERE buyerid IS NOT NULL
            )
            UPDATE messages message
            SET conversation_id = ranked.keep_id
            FROM ranked
            WHERE message.conversation_id = ranked.id
              AND ranked.rn > 1
            """);

        jdbcTemplate.execute("""
            WITH latest_message AS (
                SELECT DISTINCT ON (conversation_id)
                    conversation_id,
                    content,
                    createdat
                FROM messages
                ORDER BY conversation_id, createdat DESC NULLS LAST, id DESC
            )
            UPDATE conversations conversation
            SET lastmessagecontent = latest_message.content,
                lastmessageat = latest_message.createdat,
                updatedat = GREATEST(
                    COALESCE(conversation.updatedat, latest_message.createdat),
                    latest_message.createdat
                )
            FROM latest_message
            WHERE conversation.id = latest_message.conversation_id
            """);

        jdbcTemplate.execute("""
            WITH ranked AS (
                SELECT
                    id,
                    ROW_NUMBER() OVER (
                        PARTITION BY buyerid
                        ORDER BY updatedat DESC NULLS LAST, id DESC
                    ) AS rn
                FROM conversations
                WHERE buyerid IS NOT NULL
            )
            DELETE FROM conversations conversation
            USING ranked
            WHERE conversation.id = ranked.id
              AND ranked.rn > 1
            """);

        jdbcTemplate.execute("""
            DROP INDEX IF EXISTS conversations_unique_buyer_channel
            """);

        jdbcTemplate.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS conversations_unique_buyer
            ON conversations (buyerid)
            WHERE buyerid IS NOT NULL
            """);
    }
}
