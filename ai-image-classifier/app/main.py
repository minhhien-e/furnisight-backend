import io
from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager
from PIL import Image, UnidentifiedImageError

from .model import model_gateway
from .service import prediction_service
from .config import settings

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Load the machine learning models on startup via ModelGateway
    try:
        model_gateway.load_all()
    except Exception as e:
        print(f"Error loading models: {e}")
        raise RuntimeError(f"Failed to initialize AI models: {e}") from e
    
    yield
    # Clean up resources on shutdown if needed
    
app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    lifespan=lifespan
)

@app.get("/health")
async def health_check():
    """
    Health check endpoint to verify service is running.
    """
    if model_gateway.classifier_normal is None:
        raise HTTPException(status_code=503, detail="Service Unavailable: Model not loaded")
        
    return {"status": "ok"}

@app.post("/predict")
async def predict_image(file: UploadFile = File(...), image_type: str = Form("normal")):
    """
    Receives an image file, processes it, and returns the classification label and confidence.
    """
    # 1. Validate file extension/content type
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File provided is not an image.")
        
    try:
        # Read the file bytes
        contents = await file.read()
        # Xử lý toàn bộ logic dự đoán qua Service Layer
        try:
            result = prediction_service.process_image(contents, image_type)
        except ValueError as ve:
            # Lỗi bad input (vd form file ảnh sai)
            raise HTTPException(status_code=400, detail=str(ve))
        except RuntimeError as re:
            # Lỗi khi xử lý mô hình bên trong
            raise HTTPException(status_code=500, detail=str(re))
            
        # Trả về JSON response
        return JSONResponse(content=result)
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")
    finally:
        await file.close()
