from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    grpc_port: int = 50051
    model_id: str = "wonrax/phobert-base-vietnamese-sentiment"

    model_config = SettingsConfigDict(env_prefix="AI_REVIEW_SENTIMENT_", extra="ignore")


settings = Settings()
