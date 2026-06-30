import sys
from loguru import logger


def setup_logging(debug: bool = False) -> None:
    logger.remove()
    level = "DEBUG" if debug else "INFO"
    fmt = (
        "<green>{time:YYYY-MM-DD HH:mm:ss}</green> | "
        "<level>{level: <8}</level> | "
        "<cyan>{name}</cyan>:<cyan>{line}</cyan> — "
        "<level>{message}</level>"
    )
    logger.add(sys.stdout, level=level, format=fmt, colorize=False)
    logger.add(
        "logs/ai-service.log",
        level="INFO",
        format=fmt,
        rotation="10 MB",
        retention="7 days",
        colorize=False,
    )
    logger.info(f"Logging initialised at level={level}")
