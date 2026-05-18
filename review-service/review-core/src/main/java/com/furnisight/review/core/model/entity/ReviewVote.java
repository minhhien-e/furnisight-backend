package com.furnisight.review.core.model.entity;

import com.furnisight.review.core.model.enums.VoteType;
import com.furnisight.review.core.model.valueobject.ReviewVoteId;
import com.furnisight.review.core.exception.ReviewDomainException;
import com.furnisight.review.core.exception.enums.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "review_votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewVote {

    @EmbeddedId
    private ReviewVoteId id;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(
        name = "vote_type",
        columnDefinition = "vote_type"
    )
    private VoteType voteType;

    private LocalDateTime createdAt;

    public ReviewVote(ReviewVoteId id, VoteType voteType) {
        validate(id, voteType);
        this.id = id;
        this.voteType = voteType;
        this.createdAt = LocalDateTime.now();
    }

    public void changeVote(VoteType newType) {
        Objects.requireNonNull(newType, "New vote type cannot be null");

        if (this.voteType == newType) {
            throw new ReviewDomainException(ErrorCode.VOTE_TYPE_UNCHANGED,
                    Map.of("currentType", this.voteType));
        }

        this.voteType = newType;
    }

    private void validate(ReviewVoteId id, VoteType voteType) {
        if (id == null) {
            throw new ReviewDomainException(ErrorCode.VOTE_ID_MISSING);
        }
        if (voteType == null) {
            throw new ReviewDomainException(ErrorCode.VOTE_TYPE_MISSING);
        }
    }
}
