"""
@Date: 2021/09/19
@description: Inference-only pipeline for 3D mesh export.
"""
import os
import argparse
import cv2
import numpy as np
import torch
import glob

from tqdm import tqdm
from PIL import Image
from pipelines.lgnet.config.defaults import get_config
from pipelines.lgnet.models.build import build_model
from pipelines.lgnet.postprocessing.post_process import post_process
from pipelines.lgnet.preprocessing.pano_lsd_align import panoEdgeDetection, rotatePanorama
from pipelines.lgnet.utils.boundary import corners2boundaries, layout2depth
from pipelines.lgnet.utils.conversion import depth2xyz
from pipelines.lgnet.utils.logger import get_logger
from pipelines.lgnet.utils.misc import tensor2np
from pipelines.lgnet.visualization.obj3d import create_3d_obj


def parse_option():
    parser = argparse.ArgumentParser(description='LGT-Net mesh inference script')
    parser.add_argument('--img_glob',
                        type=str,
                        required=True,
                        help='image glob path')

    parser.add_argument('--cfg',
                        type=str,
                        required=True,
                        metavar='FILE',
                        help='path of config file')

    parser.add_argument('--post_processing',
                        type=str,
                        default='manhattan',
                        choices=['manhattan', 'atalanta', 'original'],
                        help='post-processing type')

    parser.add_argument('--output_dir',
                        type=str,
                        default='outputs/lgnet',
                        help='path of output')

    parser.add_argument('--visualize_3d', action='store_true',
                        help='Preview mesh via Open3D window')

    parser.add_argument('--output_3d', action='store_true', default=True,
                        help='Export mesh file')

    parser.add_argument('--device',
                        type=str,
                        default='cuda',
                        help='device')

    args = parser.parse_args()
    args.mode = 'test'

    print("arguments:")
    for arg in vars(args):
        print(arg, ":", getattr(args, arg))
    print("-" * 50)
    return args


def preprocess(img_ori, q_error=0.7, refine_iter=3, vp_cache_path=None):
    # Align images with VP
    if vp_cache_path is not None and os.path.exists(vp_cache_path):
        with open(vp_cache_path) as f:
            vp = [[float(v) for v in line.rstrip().split(' ')] for line in f.readlines()]
            vp = np.array(vp)
    else:
        # VP detection and line segment extraction
        _, vp, _, _, _, _, _ = panoEdgeDetection(img_ori,
                                                 qError=q_error,
                                                 refineIter=refine_iter)
    i_img = rotatePanorama(img_ori, vp[2::-1])

    if vp_cache_path is not None:
        with open(vp_cache_path, 'w') as f:
            for i in range(3):
                f.write('%.6f %.6f %.6f\n' % (vp[i, 0], vp[i, 1], vp[i, 2]))

    return i_img, vp


def inference():
    if len(img_paths) == 0:
        logger.error('No images found')
        return

    bar = tqdm(img_paths, ncols=100)
    for img_path in bar:
        if not os.path.isfile(img_path):
            logger.error(f'The {img_path} not is file')
            continue
        name = os.path.basename(img_path).split('.')[0]
        bar.set_description(name)
        img = np.array(Image.open(img_path).resize((1024, 512), Image.Resampling.BICUBIC))[..., :3]
        if args.post_processing is not None and 'manhattan' in args.post_processing:
            bar.set_description("Preprocessing")
            img, vp = preprocess(img, vp_cache_path=os.path.join(args.output_dir, f"{name}_vp.txt"))

        img = (img / 255.0).astype(np.float32)
        run_one_inference(img, model, args, name, logger)


@torch.no_grad()
def run_one_inference(img, model, args, name, logger, mesh_format='.obj', mesh_resolution=1024):
    model.eval()
    dt = model(torch.from_numpy(img.transpose(2, 0, 1)[None]).to(args.device))
    if args.post_processing != 'original':
        dt['processed_xyz'] = post_process(tensor2np(dt['depth']), type_name=args.post_processing)

    output_xyz = dt['processed_xyz'][0] if 'processed_xyz' in dt else depth2xyz(tensor2np(dt['depth'][0]))
    if args.visualize_3d or args.output_3d:
        dt_boundaries = corners2boundaries(tensor2np(dt['ratio'][0])[0], corners_xyz=output_xyz, step=None,
                                           length=mesh_resolution if 'processed_xyz' in dt else None,
                                           visible=True if 'processed_xyz' in dt else False)
        dt_layout_depth = layout2depth(dt_boundaries, show=False)

        mesh_path = os.path.join(args.output_dir, f"{name}_3d{mesh_format}") if args.output_3d else None
        create_3d_obj(
            cv2.resize(img, dt_layout_depth.shape[::-1]),
            dt_layout_depth,
            save_path=mesh_path,
            mesh=True,
            show=args.visualize_3d,
        )
        return mesh_path
    return None


if __name__ == '__main__':
    logger = get_logger()
    args = parse_option()
    config = get_config(args)

    if ('cuda' in args.device or 'cuda' in config.TRAIN.DEVICE) and not torch.cuda.is_available():
        logger.info(f'The {args.device} is not available, will use cpu ...')
        config.defrost()
        args.device = "cpu"
        config.TRAIN.DEVICE = "cpu"
        config.freeze()

    model, _, _, _ = build_model(config, logger)
    os.makedirs(args.output_dir, exist_ok=True)
    img_paths = sorted(glob.glob(args.img_glob))

    inference()

