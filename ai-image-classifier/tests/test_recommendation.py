import grpc

from app.recommendation import RecommendationService
from v1.catalog import catalog_message_pb2


class Stub:
    def __init__(self, response=None, error=None):
        self.response = response
        self.error = error
        self.request = None
        self.timeout = None

    def SearchRecommendedProducts(self, request, timeout):
        self.request = request
        self.timeout = timeout
        if self.error:
            raise self.error
        return self.response


class UnavailableRpcError(grpc.RpcError):
    pass


def service(stub):
    return RecommendationService(
        stub=stub,
        limit=6,
        timeout_seconds=3,
        category_mapping={"bedroom": "bedroom"},
    )


def test_returns_price_model_and_default_variant():
    product = catalog_message_pb2.RecommendedProduct(
        id="product-1",
        slug="bed",
        name="Bed",
        category_name="Bedroom",
        price=9500000,
        image="https://example.com/bed.jpg",
        model_url="https://example.com/bed.glb",
        default_variant_id="variant-1",
        rating=4.5,
        rating_count=10,
        sold_count=3,
        tags=["new"],
    )
    stub = Stub(catalog_message_pb2.SearchRecommendedProductsResponse(
        products=[product]
    ))

    result = service(stub).recommend_for_label("bedroom")

    assert result["recommendations"][0]["price"] == 9500000
    assert result["recommendations"][0]["modelUrl"].endswith(".glb")
    assert result["recommendations"][0]["defaultVariantId"] == "variant-1"
    assert stub.request.category_slug == "bedroom"
    assert stub.request.status == "ACTIVE"
    assert stub.timeout == 3


def test_preserves_missing_price_as_none():
    product = catalog_message_pb2.RecommendedProduct(
        id="product-1",
        slug="bed",
        name="Bed",
    )
    stub = Stub(catalog_message_pb2.SearchRecommendedProductsResponse(
        products=[product]
    ))

    result = service(stub).recommend_for_label("bedroom")

    assert result["recommendations"][0]["price"] is None


def test_catalog_failure_does_not_fail_prediction():
    result = service(Stub(error=UnavailableRpcError())).recommend_for_label(
        "bedroom"
    )

    assert result["recommendations"] == []
    assert result["recommendationMeta"]["reason"] == "catalog_unavailable"
