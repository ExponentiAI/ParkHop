import numpy as np


class Wmv_Linear_Model:
    def __init__(self, data, trainset, train_ans, labels):
        def varFun(c: int, n: int):
            w = c / n
            var = c * (n - c) / (n ** 2 * (n - 1))
            return var
        self.train_ans = train_ans
        self.n_work = data.shape[1]
        self.n_items = data.shape[0] - len(trainset)
        self.correct_nums = np.zeros((self.n_work))
        for i in range(self.n_work):
            self.correct_nums[i] = len(
                [j for j in data.T[i, :len(trainset)] if j == 1])
        self.control_nums = len(trainset)
        self.labels = labels
        self.L = data[len(trainset):]
        self.wmv_vars = np.vectorize(varFun)(self.correct_nums, len(trainset))
        self.wmv_linear()

    def wmv_linear(self):
        """
        wmv-linear 聚合算法
        :param correct_nums: 已知问题答对个数，列表
        :param control_nums: 已知问题的个数，列表（每个人不一定一样）
        :param L1: 未知问题的回答
        :param label:待选项
        :return: 估计值
        """
        # 加权权重
        f_w = len(self.labels) * (self.correct_nums / self.control_nums) - 1
        self.mu = np.zeros([self.n_items])
        for i in range(self.n_items):
            max_i = 0
            opts = set(self.L[i])
            for la in opts:
                tmp = 0
                for s in range(self.n_work):
                    if self.L[i][s] == la:
                        tmp += f_w[s]
                if tmp > max_i:
                    max_i = tmp
                    self.mu[i] = la
#         print("wmv-linear mu:", self.mu)
