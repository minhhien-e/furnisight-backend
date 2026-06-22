ALTER TABLE promotions DROP COLUMN IF EXISTS placements;
ALTER TABLE promotion_combos DROP COLUMN IF EXISTS placements;

CREATE INDEX IF NOT EXISTS idx_promotions_public_window
    ON promotions (voucher_type, active, end_date, code);
CREATE INDEX IF NOT EXISTS idx_user_vouchers_user_unused
    ON user_vouchers (user_id, is_used, promotion_id);
CREATE INDEX IF NOT EXISTS idx_promotion_combos_active_window
    ON promotion_combos (active, saved_amount DESC, used_count DESC, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_promotion_combo_items_combo
    ON promotion_combo_items (combo_id);
