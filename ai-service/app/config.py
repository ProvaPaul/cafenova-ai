from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    app_name: str = "SmartCafe AI Service"
    app_version: str = "2.0.0"
    app_host: str = "0.0.0.0"
    app_port: int = 8000
    debug: bool = False

    cors_origins: str = "http://localhost:5173,http://localhost:3000,http://localhost:8080"
    backend_url: str = "http://localhost:8080"

    recommendation_limit: int = 5
    confidence_min: float = 0.30
    analytics_enabled: bool = True

    # MySQL connection
    db_host: str = "localhost"
    db_port: int = 3307
    db_name: str = "smart_cafe_db"
    db_user: str = "root"
    db_password: str = ""

    # Apriori training parameters
    train_min_support: float = 0.02
    train_min_confidence: float = 0.30
    train_max_len: int = 3

    # Scheduled retrain hour (0 = disabled)
    auto_retrain_hour: int = 3

    @property
    def allowed_origins(self) -> list[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()
