from typing import Any, Dict, List, Optional

import requests

from .config import settings


class RecommendationService:
    """
    Fetches catalog products that match the predicted room category.
    Recommendation failures are non-blocking so image classification can still succeed.
    """

    SOURCE = "catalog-service"

    def __init__(
        self,
        catalog_base_url: Optional[str] = None,
        limit: Optional[int] = None,
        timeout_seconds: Optional[float] = None,
        category_mapping: Optional[Dict[str, str]] = None,
        http_client: Any = requests,
    ):
        self.catalog_base_url = (catalog_base_url or settings.CATALOG_BASE_URL).rstrip("/")
        self.limit = limit if limit is not None else settings.RECOMMENDATION_LIMIT
        self.timeout_seconds = (
            timeout_seconds
            if timeout_seconds is not None
            else settings.RECOMMENDATION_TIMEOUT_SECONDS
        )
        self.category_mapping = category_mapping or settings.CATEGORY_MAPPING
        self.http_client = http_client

    def recommend_for_label(self, label: str) -> Dict[str, Any]:
        category_slug = self._resolve_category_slug(label)
        if category_slug is None:
            return self._empty_response(None, "category_not_mapped")

        try:
            response = self.http_client.get(
                f"{self.catalog_base_url}/products",
                params={
                    "category": category_slug,
                    "status": "ACTIVE",
                    "size": self.limit,
                    "sort": "newest",
                },
                timeout=self.timeout_seconds,
            )
            response.raise_for_status()
        except requests.RequestException:
            return self._empty_response(category_slug, "catalog_unavailable")

        products = self._extract_products(response)
        if not products:
            return self._empty_response(category_slug, "no_products_found")

        return {
            "recommendations": products,
            "recommendationMeta": {
                "categorySlug": category_slug,
                "source": self.SOURCE,
                "reason": None,
            },
        }

    def _resolve_category_slug(self, label: str) -> Optional[str]:
        if label is None:
            return None

        normalized_label = label.strip().lower()
        return self.category_mapping.get(normalized_label)

    def _extract_products(self, response: requests.Response) -> List[Dict[str, Any]]:
        try:
            payload = response.json()
        except ValueError:
            return []

        raw_products = payload.get("products", [])
        if not isinstance(raw_products, list):
            return []

        return [self._normalize_product(product) for product in raw_products if isinstance(product, dict)]

    def _normalize_product(self, product: Dict[str, Any]) -> Dict[str, Any]:
        return {
            "id": product.get("id"),
            "slug": product.get("slug"),
            "name": product.get("name"),
            "categoryName": product.get("categoryName"),
            "price": product.get("price"),
            "oldPrice": product.get("oldPrice"),
            "image": product.get("image"),
            "rating": product.get("rating"),
            "ratingCount": product.get("ratingCount"),
            "soldCount": product.get("soldCount"),
            "tags": product.get("tags") or [],
        }

    def _empty_response(self, category_slug: Optional[str], reason: str) -> Dict[str, Any]:
        return {
            "recommendations": [],
            "recommendationMeta": {
                "categorySlug": category_slug,
                "source": self.SOURCE,
                "reason": reason,
            },
        }


recommendation_service = RecommendationService()
