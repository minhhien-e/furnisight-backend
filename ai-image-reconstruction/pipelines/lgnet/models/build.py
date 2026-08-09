"""
Inference-only model builder.
"""
import os

import pipelines.lgnet.models as models
from pipelines.lgnet.utils.time_watch import TimeWatch


def build_model(config, logger):
    name = config.MODEL.NAME
    TimeWatch(f"Build model: {name}", logger)

    device = config.TRAIN.DEVICE
    logger.info(f"Creating model: {name} to device:{device}, args:{config.MODEL.ARGS[0]}")

    net = getattr(models, name)
    ckpt_dir = os.path.abspath(os.path.join(config.CKPT.DIR, os.pardir)) if config.DEBUG else config.CKPT.DIR
    if len(config.MODEL.ARGS) != 0:
        model = net(ckpt_dir=ckpt_dir, **config.MODEL.ARGS[0])
    else:
        model = net(ckpt_dir=ckpt_dir)

    logger.info(f"model dropout: {model.dropout_d}")
    model = model.to(device)

    config.defrost()
    config.TRAIN.START_EPOCH = model.load(device, logger)
    config.freeze()

    model.show_parameter_number(logger)

    # Preserve the old return shape so existing inference callers stay unchanged.
    return model, None, {}, None

