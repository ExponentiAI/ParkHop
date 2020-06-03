from math import sqrt
import numpy as np

def gFun(c: int, n: int, L: int):
    w = c / n
    var = c * (n - c) / (n ** 2 * (n - 1))
    return (L * w - 1) ** 2 - L ** 2 * var

def select(reliabilities, max_workers: int, control_num: np.array, label_num: int):
    """

    :param label_num: 可选项数量
    :param reliabilities: 在先验过程中正确回答问题的数目
    :param max_workers: 限制的最大工作者数量
    :param control_num: 预置问题数量
    :return: 返回需要的众包人员编号集合
    """

    # 计算每个worker的 G( ˆω_i)并 赋值给 x_i  ,然后倒序排列（从大到小）
    reliabilities = np.vectorize(gFun)(reliabilities, control_num, label_num)
    indict = np.argsort(reliabilities)[::-1]  # 排序后的原始列表编号
    reliabilities[::-1].sort()  # 原地排序
    pool_number = len(reliabilities)

    # 初始化
    b = min(pool_number, max_workers)
    assert b > 1
    k = 1
    g_sum = reliabilities[0]
    f = g_sum
    maxf = f
    # 接下来开始循环，数量k 从2到b 计算F(K)的值，直到满足F 最大的K值出现，最后选择一个满足的最小的k值
    for i in range(1, b):
        g_sum += reliabilities[i]
        f = g_sum / sqrt(i)
        if f > maxf :
            maxf = f
            k = i

    # 满足条件的工作者子集(编号)
    return indict[:k]