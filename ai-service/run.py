"""Entry point — run with: python run.py"""
import uvicorn
from app.config import get_settings

if __name__ == "__main__":
    s = get_settings()
    uvicorn.run(
        "app.main:app",
        host=s.app_host,
        port=s.app_port,
        reload=s.debug,
        log_level="debug" if s.debug else "info",
    )
