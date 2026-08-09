from __future__ import annotations

from dataclasses import dataclass
import logging
import math
import re
import threading
import unicodedata
from typing import Iterable

logger = logging.getLogger(__name__)


@dataclass
class SentimentResult:
    label: str
    confidence: float


class PhoBertSentimentEngine:
    def __init__(self, model_id: str) -> None:
        self.model_id = model_id
        self._pipeline = None
        self._load_error = None
        self._load_lock = threading.Lock()
        self._loading = False
        self._start_background_load()

    def _start_background_load(self) -> None:
        if self._pipeline is not None or self._loading:
            return

        self._loading = True

        def run() -> None:
            try:
                self._try_load()
            finally:
                self._loading = False

        threading.Thread(target=run, name="phobert-loader", daemon=True).start()

    def _try_load(self) -> None:
        with self._load_lock:
            if self._pipeline is not None:
                return
            try:
                from transformers import pipeline

                self._pipeline = pipeline(
                    "text-classification",
                    model=self.model_id,
                    tokenizer=self.model_id,
                    truncation=True,
                )
                logger.info("Loaded PhoBERT sentiment model: %s", self.model_id)
            except Exception as exc:  # pragma: no cover
                self._load_error = exc
                logger.warning("Falling back to heuristic sentiment engine: %s", exc)

    def analyze(self, text: str) -> SentimentResult:
        text = (text or "").strip()
        if not text:
            return SentimentResult("NEUTRAL", 0.5)

        if self._pipeline is not None:
            result = self._pipeline(text, top_k=1)[0]
            return SentimentResult(
                label=self._normalize_label(result.get("label")),
                confidence=float(result.get("score", 0.0)),
            )

        if not self._loading and self._pipeline is None:
            self._start_background_load()

        return self._heuristic(text)

    def _normalize_label(self, label: str | None) -> str:
        raw = (label or "").upper().strip()
        if raw in {"POS", "POSITIVE", "LABEL_2", "5 STARS", "4 STARS"} or "POS" in raw:
            return "POSITIVE"
        if raw in {"NEG", "NEGATIVE", "LABEL_0", "1 STAR", "2 STARS"} or "NEG" in raw:
            return "NEGATIVE"
        if raw in {"NEU", "NEUTRAL", "LABEL_1", "3 STARS"} or "NEU" in raw:
            return "NEUTRAL"
        return "NEUTRAL"

    def _heuristic(self, text: str) -> SentimentResult:
        normalized = self._normalize_text(text)

        positive_words = {
            "đẹp",
            "tốt",
            "thích",
            "rất thích",
            "hài lòng",
            "tuyệt vời",
            "xuất sắc",
            "xinh",
            "ok",
            "chất lượng",
            "đáng tiền",
            "thoải mái",
            "chắc chắn",
            "mềm",
            "giao hàng nhanh",
            "đóng gói cẩn thận",
            "good",
            "great",
            "excellent",
            "nice",
            "love",
            "perfect",
            "satisfied",
        }
        negative_words = {
            "tệ",
            "xấu",
            "chậm",
            "lỗi",
            "kém",
            "hỏng",
            "thất vọng",
            "không ổn",
            "không thích",
            "quá tệ",
            "giao hàng chậm",
            "đóng gói kém",
            "bị vỡ",
            "nứt vỡ",
            "rách",
            "bad",
            "slow",
            "broken",
            "poor",
            "disappointed",
            "terrible",
        }

        score = self._count_hits(normalized, positive_words) - self._count_hits(normalized, negative_words)
        if score > 0:
            return SentimentResult("POSITIVE", min(0.55 + math.log1p(score) * 0.1, 0.95))
        if score < 0:
            return SentimentResult("NEGATIVE", min(0.55 + math.log1p(abs(score)) * 0.1, 0.95))
        return SentimentResult("NEUTRAL", 0.6)

    @staticmethod
    def _count_hits(text: str, words: Iterable[str]) -> int:
        return sum(
            1
            for word in words
            if re.search(
                rf"(?<!\w){re.escape(PhoBertSentimentEngine._normalize_text(word))}(?!\w)",
                text,
            )
        )

    @staticmethod
    def _normalize_text(text: str) -> str:
        lowered = text.lower().strip().replace("đ", "d")
        normalized = unicodedata.normalize("NFKD", lowered)
        ascii_text = "".join(char for char in normalized if not unicodedata.combining(char))
        return " ".join(ascii_text.split())
