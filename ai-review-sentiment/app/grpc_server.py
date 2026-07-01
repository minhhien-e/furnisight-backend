from __future__ import annotations

from concurrent import futures
import grpc
import logging
import threading

from app.config import settings
from app.sentiment import PhoBertSentimentEngine
from v1.ai import review_sentiment_pb2
from v1.ai import review_sentiment_pb2_grpc

logger = logging.getLogger(__name__)


class ReviewSentimentServicer(review_sentiment_pb2_grpc.ReviewSentimentServiceServicer):
    def __init__(self, engine: PhoBertSentimentEngine) -> None:
        self.engine = engine

    def AnalyzeReviewSentiment(self, request, context):
        result = self.engine.analyze(request.text)
        return review_sentiment_pb2.AnalyzeReviewSentimentResponse(
            sentiment=result.label,
            confidence=result.confidence,
        )


def start_grpc_server() -> grpc.Server:
    engine = PhoBertSentimentEngine(settings.model_id)
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=4))
    review_sentiment_pb2_grpc.add_ReviewSentimentServiceServicer_to_server(
        ReviewSentimentServicer(engine),
        server,
    )
    server.add_insecure_port(f"0.0.0.0:{settings.grpc_port}")
    server.start()
    logger.info("AI review sentiment gRPC listening on %s", settings.grpc_port)
    return server


def start_grpc_server_in_thread() -> grpc.Server:
    holder = {}

    def run() -> None:
        holder["server"] = start_grpc_server()
        holder["server"].wait_for_termination()

    thread = threading.Thread(target=run, daemon=True)
    thread.start()
    return holder
