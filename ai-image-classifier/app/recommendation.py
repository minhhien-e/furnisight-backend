from typing import Any, Dict, Optional

import grpc

from v1.catalog import catalog_message_pb2, catalog_pb2_grpc

from .config import settings


class RecommendationService:
    """Fetches recommendation products from Catalog over gRPC."""

    SOURCE = "catalog-service"

    def __init__(
        self,
        catalog_grpc_target: Optional[str] = None,
        limit: Optional[int] = None,
        timeout_seconds: Optional[float] = None,
        category_mapping: Optional[Dict[str, str]] = None,
        stub: Any = None,
        channel_factory: Any = grpc.insecure_channel,
    ):
        raw_target = catalog_grpc_target or settings.CATALOG_SERVICE_GRPC_ADDRESS
        self.catalog_grpc_target = raw_target.replace("dns:///", "").replace("static://", "")
        self.limit = limit if limit is not None else settings.RECOMMENDATION_LIMIT
        self.timeout_seconds = (
            timeout_seconds
            if timeout_seconds is not None
            else settings.RECOMMENDATION_TIMEOUT_SECONDS
        )
        self.category_mapping = category_mapping or settings.CATEGORY_MAPPING
        self._channel = None
        if stub is not None:
            self.stub = stub
        else:
            self._channel = channel_factory(self.catalog_grpc_target)
            self.stub = catalog_pb2_grpc.CatalogServiceStub(self._channel)

    def recommend_for_label(self, label: str) -> Dict[str, Any]:
        category_slug = self._resolve_category_slug(label)
        if category_slug is None:
            return self._empty_response(None, "category_not_mapped")

        request = catalog_message_pb2.SearchRecommendedProductsRequest(
            category_slug=category_slug,
            limit=self.limit,
            status="ACTIVE",
        )
        try:
            response = self.stub.SearchRecommendedProducts(
                request,
                timeout=self.timeout_seconds,
            )
        except grpc.RpcError:
            return self._empty_response(category_slug, "catalog_unavailable")

        products = [self._normalize_product(product) for product in response.products]
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
        return self.category_mapping.get(label.strip().lower())

    def _normalize_product(self, product: Any) -> Dict[str, Any]:
        variants = [
            {
                "id": v.id,
                "price": v.price if v.HasField("price") else None,
                "stockQuantity": v.stock_quantity if v.HasField("stock_quantity") else None,
                "length": v.length if v.HasField("length") else None,
                "width": v.width if v.HasField("width") else None,
                "height": v.height if v.HasField("height") else None,
                "weight": v.weight if v.HasField("weight") else None,
                "color": v.color,
                "material": v.material,
                "warranty": v.warranty,
                "modelUrl": v.model_url,
                "supports3d": getattr(v, "supports3d", bool(v.model_url)),
                "imageUrls": list(getattr(v, "image_urls", [])),
            }
            for v in product.variants
        ] if hasattr(product, "variants") else []
        default_variant = self._resolve_default_variant(
            variants,
            product.default_variant_id,
            product.price if product.HasField("price") else None,
        )

        return {
            "id": product.id,
            "slug": product.slug,
            "name": product.name,
            "categoryName": product.category_name,
            "price": product.price if product.HasField("price") else None,
            "image": product.image,
            "defaultVariantId": product.default_variant_id,
            "defaultVariant": default_variant,
            "variantId": product.default_variant_id,
            "rating": product.rating,
            "ratingCount": product.rating_count,
            "soldCount": product.sold_count,
            "variants": variants,
        }

    def _resolve_default_variant(
        self,
        variants: list,
        default_variant_id: str,
        fallback_price: Optional[float],
    ) -> Optional[Dict[str, Any]]:
        if not variants:
            return None
        for variant in variants:
            if variant["id"] == default_variant_id:
                return variant
        variant = variants[0].copy()
        if variant.get("price") is None:
            variant["price"] = fallback_price
        return variant

    def _empty_response(
        self, category_slug: Optional[str], reason: str
    ) -> Dict[str, Any]:
        return {
            "recommendations": [],
            "recommendationMeta": {
                "categorySlug": category_slug,
                "source": self.SOURCE,
                "reason": reason,
            },
        }


recommendation_service = RecommendationService()
