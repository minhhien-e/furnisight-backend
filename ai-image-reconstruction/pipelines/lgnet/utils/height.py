"""
@date: 2021/6/30
@description:
"""
import numpy as np
from typing import List

from pipelines.lgnet.utils.boundary import *
from scipy.optimize import least_squares
from functools import partial


def lsq_fit(ceil_norm, floor_norm):
    """
    Least Squares
    :param ceil_norm:
    :param floor_norm:
    :return:
    """

    def error_fun(ratio, ceil_norm, floor_norm):
        error = np.abs(ratio * ceil_norm - floor_norm)
        return error

    init_ratio = np.mean(floor_norm / ceil_norm, axis=-1)
    error_func = partial(error_fun, ceil_norm=ceil_norm, floor_norm=floor_norm)
    ret = least_squares(error_func, init_ratio, verbose=0)
    ratio = ret.x[0]
    return ratio


def mean_percentile_fit(ceil_norm, floor_norm, p1=25, p2=75):
    """
    :param ceil_norm:
    :param floor_norm:
    :param p1:
    :param p2:
    :return:
    """
    ratio = floor_norm / ceil_norm
    r_min = np.percentile(ratio, p1)
    r_max = np.percentile(ratio, p2)
    return ratio[(r_min <= ratio) & (ratio <= r_max)].mean()


def calc_ceil_ratio(boundaries: List[np.array], mode='lsq'):
    """
    :param boundaries: [ [[cu1, cv1], [cu2, cv2], ...], [[fu1, fv1], [fu2, fv2], ...] ]
    :param mode: 'lsq' or 'mean'
    :return:
    """
    assert len(boundaries[0].shape) < 4 and len(boundaries[1].shape) < 4, 'error shape'
    if not is_normal_layout(boundaries):
        return 0

    ceil_boundary = boundaries[0]
    floor_boundary = boundaries[1]
    assert ceil_boundary.shape[-2] == floor_boundary.shape[-2], "boundary need same length"

    ceil_xyz = uv2xyz(ceil_boundary, -1)
    floor_xyz = uv2xyz(floor_boundary, 1)

    ceil_xz = ceil_xyz[..., ::2]
    floor_xz = floor_xyz[..., ::2]

    ceil_norm = np.linalg.norm(ceil_xz, axis=-1)
    floor_norm = np.linalg.norm(floor_xz, axis=-1)

    if mode == "lsq":
        if len(ceil_norm.shape) == 2:
            ratio = np.array([lsq_fit(ceil_norm[i], floor_norm[i]) for i in range(ceil_norm.shape[0])])
        else:
            ratio = lsq_fit(ceil_norm, floor_norm)
    else:
        if len(ceil_norm.shape) == 2:
            ratio = np.array([mean_percentile_fit(ceil_norm[i], floor_norm[i]) for i in range(ceil_norm.shape[0])])
        else:
            ratio = mean_percentile_fit(ceil_norm, floor_norm)

    return ratio


def calc_ceil_height(boundaries: List[np.array], camera_height=1.6, mode='lsq') -> float:
    """
    :param boundaries: [ [[cu1, cv1], [cu2, cv2], ...], [[fu1, fv1], [fu2, fv2], ...] ]
    :param camera_height:
    :param mode:
    :return:
    """
    ratio = calc_ceil_ratio(boundaries, mode)
    ceil_height = camera_height * ratio
    return ceil_height


def calc_room_height(boundaries: List[np.array], camera_height=1.6, mode='lsq') -> float:
    """
    :param boundaries: also can cornersï¼Œformat: [ [[cu1, cv1], [cu2, cv2], ...], [[fu1, fv1], [fu2, fv2], ...] ],
    0 denotes ceil, 1 denotes floor
    :param camera_height: actual camera height determines the scale
    :param mode: fitting method lsq or mean
    :return:
    """
    ceil_height = calc_ceil_height(boundaries, camera_height, mode)
    room_height = camera_height + ceil_height
    return room_height


def height2ratio(height, camera_height=1.6):
    ceil_height = height - camera_height
    ratio = ceil_height / camera_height
    return ratio


def ratio2height(ratio, camera_height=1.6):
    ceil_height = camera_height * ratio
    room_height = camera_height + ceil_height
    return room_height

