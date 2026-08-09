"""
Base model utilities for inference checkpoints.
"""
import os

import torch
import torch.nn as nn


class BaseModule(nn.Module):
    def __init__(self, ckpt_dir=None):
        super().__init__()
        self.ckpt_dir = ckpt_dir
        self.model_lst = []
        self.best_model_path = None

        if ckpt_dir:
            os.makedirs(ckpt_dir, exist_ok=True)
            self.model_lst = [x for x in sorted(os.listdir(self.ckpt_dir)) if x.endswith(".pkl")]

    def show_parameter_number(self, logger):
        total = sum(p.numel() for p in self.parameters())
        trainable = sum(p.numel() for p in self.parameters() if p.requires_grad)
        logger.info("{} parameter total:{:,}, trainable:{:,}".format(self._get_name(), total, trainable))

    def load(self, device, logger):
        if len(self.model_lst) == 0:
            logger.info("*" * 50)
            logger.info("Empty model folder! Using initial weights")
            logger.info("*" * 50)
            return 0

        best_model_lst = [name for name in self.model_lst if name == "best.pkl" or "_best_" in name]
        ckpt_name = best_model_lst[-1] if len(best_model_lst) > 0 else self.model_lst[0]
        ckpt_path = os.path.join(self.ckpt_dir, ckpt_name)
        self.best_model_path = ckpt_path

        logger.info("*" * 50)
        logger.info(f"Load checkpoint: {ckpt_path}")
        checkpoint = torch.load(ckpt_path, map_location=torch.device(device))

        # Support both converted inference checkpoints and old training checkpoints.
        state_dict = checkpoint["net"] if isinstance(checkpoint, dict) and "net" in checkpoint else checkpoint
        self.load_state_dict(state_dict, strict=False)
        logger.info("*" * 50)

        if isinstance(checkpoint, dict) and "epoch" in checkpoint:
            return checkpoint["epoch"] + 1
        return 0

