from django.shortcuts import render
import json
from django.http import HttpResponse
from math import sqrt
from parking.models import *
import numpy as np
import sys
import time
from numpy import amax, tile, transpose, mean, dot


# Create your views here.
class JointMLEGaussian:
    def __init__(self, L, traindx, trainp, model=0, maxIter=500, TOL=1e-7):
        # model: 0:bias_variance, 1:bias_variance+task, 2:bias_variance+worker, 3:bias_variance+workder+task
        self.L = L
        self.Ntask = self.L.shape[0]
        self.Nwork = self.L.shape[1]
        self.model = model
        self.maxIter = maxIter
        self.TOL = TOL

        # parameters init
        self.true_labels = np.array([])
        # mu_prior, w_mu_prior, nu_prior, w_nu_prior
        self.mu_prior = np.zeros([self.Ntask, 1])
        self.w_mu_prior = np.zeros([self.Ntask, 1])
        self.nu_prior = np.zeros([1, self.Nwork])
        self.w_nu_prior = np.zeros([1, self.Nwork])

        self.ptrue = np.ones([self.Ntask, 1])+1
        self.traindx = traindx
        self.trainp = trainp
        self.testdx = np.array(self.setdiff1d(np.arange(1, self.Ntask + 1), self.traindx), dtype=int)
        # initialize
        self.w_mu = np.ones([self.Ntask, 1])
        self.w_nu = np.ones([1, self.Nwork])
        self.mu = np.array([self.L.mean(axis=1)]).transpose()
        self.mu[self.traindx - 1, 0] = self.trainp
        self.nu = np.zeros([1, self.Nwork])
        if model < 0 or model > 3:
            exit(0)
        self.update_mu = True
        self.update_nu = True
        if model != 1:
            self.update_w_nu = True
            if model != 2:
                self.update_w_mu = True
        else:
            self.update_w_mu = True
        self.A = np.zeros(L.shape)
        self.A[(L != 0).nonzero()] = 1
        self.DegWork = self.A.sum(axis=0)
        self.DegTask = np.array([self.A.sum(axis=1)]).transpose()
        self.mseVec = np.zeros(self.maxIter,dtype=float)  # one dim
        self.errVec = np.zeros(self.maxIter)  # one dim
        self.posteriorVec = np.zeros(self.maxIter)  # one dim
        self.main_iteration()

    def main_iteration(self):
        for iter in range(100):
            self.old_mu = self.mu  # Ntask*1
            self.old_nu = self.nu  # 1*Nwork
            self.old_w_mu = self.w_mu  # Ntask*1
            self.old_w_nu = self.w_nu  # 1*Nwork
            if self.update_mu:
                self.mu[self.testdx - 1] = (self.w_mu[self.testdx - 1] * (((self.L[self.testdx - 1,] - np.ones(
                    [self.testdx.size, 1]) * self.nu) * self.A[self.testdx - 1,]).dot(self.w_nu.transpose())) \
                                            + self.mu_prior[self.testdx - 1] * self.w_mu_prior[self.testdx - 1]) / ((
                                                                                                                            self.w_mu[
                                                                                                                                self.testdx - 1] * (
                                                                                                                                self.A[
                                                                                                                                    self.testdx - 1,].dot(
                                                                                                                                    self.w_nu.transpose()))) +
                                                                                                                    self.w_mu_prior[
                                                                                                                        self.testdx - 1])

            if self.update_nu:
                self.nu = (self.w_nu * (self.w_mu.transpose().dot(
                    (self.L - self.mu * np.ones([1, self.Nwork])) * self.A)) + self.nu_prior * self.w_nu_prior) \
                          / (self.w_nu * (self.w_mu.transpose().dot(self.A) + self.w_nu_prior))

            weps = 1e-10
            if self.update_w_mu:
                self.w_mu = 1 / (((((self.L - self.mu * np.ones([1, self.Nwork]) - np.ones(
                    [self.Ntask, 1]) * self.nu) * self.A) ** 2).dot(self.w_nu.transpose())) / self.DegTask + weps)

            if self.update_w_nu:
                self.w_nu = 1 / (self.w_mu.transpose().dot(((self.L - self.mu * np.ones([1, self.Nwork]) - np.ones(
                    [self.Ntask, 1]) * self.nu) * self.A) ** 2) / self.DegWork + weps)

            if self.ptrue.size > 0:
                self.mseVec[iter] = ((self.mu - self.ptrue) ** 2).mean(dtype=float)

            self.cal_posterior_log_likelihood()

            self.posteriorVec[iter] = self.loglikelihood + self.logprior

            # print(self.loglikelihood + self.logprior)

            # calculate posterior log likelihood

            # check convergence error
            eps = 2.2204e-16
            err = max([max(abs(self.old_mu - self.mu)) / (max(abs(self.mu)) + eps),
                       max(max(abs(self.old_nu - self.nu))) / (max(max(abs(self.nu))) + eps),
                       max(abs(self.old_w_mu - self.w_mu)) / (max(abs(self.w_mu)) + eps),
                       max(max(abs(self.old_w_nu - self.w_nu))) / (max(max(abs(self.w_nu))) + eps)])
            self.errVec[iter] = err

            # print(err)
            if err < self.TOL and iter > 1:
                break

        self.mseVec = self.mseVec[0:iter + 1]
        self.errVec = self.errVec[0:iter + 1]
        self.posteriorVec = self.posteriorVec[0:iter + 1]

    def setdiff1d(self, a, b):
        diff = np.zeros(a.size)
        len = 0
        for i in a:
            if i not in b:
                diff[len] = i
                len = len + 1
        return diff[0:len]

    def cal_posterior_log_likelihood(self):
        self.loglikelihood = -0.5 * self.w_mu.transpose().dot(
            ((self.L - self.mu * np.ones([1, self.Nwork]) - np.ones([self.Ntask, 1]) * self.nu) * self.A) ** 2).dot(
            self.w_nu.transpose()) \
                             + 0.5 * sum(np.log(self.w_mu) * self.DegTask) + 0.5 * sum(
            (np.log(self.w_nu) * self.DegWork)[0])
        self.logprior = -0.5 * sum((self.mu - self.mu_prior) ** 2 * self.w_mu_prior) - 0.5 * sum(
            ((self.nu - self.nu_prior) ** 2 * self.w_nu_prior)[0])


def gFun(c: int, n: int, L: int):
    w = c / n
    print("c ",c ," n ",n, w)
    var = c * (n - c) / (n ** 2 * (n - 1))
    return (L * w - 1) ** 2 - L ** 2 * var


def select(reliabilities, worker_parkinglot, max_workers: int, control_num: np.array, label_num: int):
    """

    :param worker_parkinglot: 工作者回答的未知问题集合
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
    plot_cover = set()
    max_cover_num = 0  # 当前目标问题的最大个数
    # 接下来开始循环，数量k 从2到b 计算F(K)的值，直到满足F 最大的K值出现，最后选择一个满足的最小的k值
    for i in range(1, b):
        g_sum += reliabilities[i]

        # print(g_sum/sqrt(i))
        f = g_sum / sqrt(i)
        plot_cover = plot_cover | worker_parkinglot[indict[i - 1]]
        # print("cover:",plot_cover)
        if f > maxf :
            if f > maxf:
                maxf = f
            k = i
            max_cover_num = len(plot_cover)

    # 满足条件的工作者子集(编号)
    return indict[:k], plot_cover


def process_answers():
    answers = Answer.objects.all().order_by('create_time')

    question_index = {}  # name:index
    crowd_sourcer_index = {}  # id:index
    parkinglot_index = {}  # id:index

    q_index = 0
    c_index = 0
    # p_index = 0

    c_time = int(round(time.time() * 1000))

    # 有效数据集合
    active_answers = []
    reliabilities = {}  # 每个worker回答正确的poi数量
    control_num = {}  # 每个worker回答的poi数量，从而计算wi
    for answer in answers:
        # 只取10小时内的数据，视为有效数据
        # if c_time - int(time.mktime(answer.create_time.timetuple()) * 1000) < 10 * 24 * 60 * 60 * 1000:
        active_answers.append(answer)
        if answer.name not in question_index:
            question_index[answer.name] = q_index
            q_index = q_index + 1
        if answer.sourcer.id not in crowd_sourcer_index:
            if len(crowd_sourcer_index) > 35:
                break
            crowd_sourcer_index[answer.sourcer.id] = c_index
            reliabilities[c_index] = 0
            control_num[c_index] = 0
            c_index = c_index + 1

        # if answer.parkinglot.category.id not in parkinglot_index:
        #     parkinglot_index[answer.parkinglot.category.id] = p_index
        #     p_index = p_index + 1
        if answer.answer == 1:
            reliabilities[crowd_sourcer_index[answer.sourcer.id]] += 1
        control_num[crowd_sourcer_index[answer.sourcer.id]] += 1

    worker_parkinglot = {}  # 每个工作者回答了的未知问题
    parkinglot_lots = ParkingLot.objects.all()

    for parkinglot in parkinglot_lots:  # 工作者提交的停车位信息
        if parkinglot.sourcer.id not in crowd_sourcer_index:
            continue
        tmp_id = crowd_sourcer_index[parkinglot.sourcer.id]
        if tmp_id not in worker_parkinglot:
            worker_parkinglot[tmp_id] = {parkinglot.category.id}  # set进停车位区域号
        else:
            worker_parkinglot[tmp_id].add(parkinglot.category.id)

    sourcer_size = len(crowd_sourcer_index)
    ret, plot_cover = select(np.fromiter(reliabilities.values(), dtype=int), worker_parkinglot, sourcer_size,
                 np.fromiter(control_num.values(), dtype=int), 2)
    # print('需要众包人员数量共%s人' % len(ret))
    # print('相应的编号如下：%s' % ret)

    q_index=0
    p_index=0
    new_active_answers = []

    for answer in active_answers :
        if  answer.sourcer_id in crowd_sourcer_index and crowd_sourcer_index[answer.sourcer.id] in ret:
            new_active_answers.append(answer)
            if answer.name not in question_index:
                question_index[answer.name] = q_index
                q_index = q_index + 1

    for plot in plot_cover:
        if plot not in parkinglot_index :
            parkinglot_index[plot] = p_index
            p_index = p_index + 1
    question_size = len(question_index)
    parkinglot_size = len(parkinglot_index)

    if question_size > 0:

        Nwork = len(ret)
        # Nwork = sourcer_size
        Ntask = question_size + 2 * parkinglot_size

        trainset = np.ones(question_size, dtype=int)
        ans_train = np.ones(question_size, dtype=int)

        L0 = np.zeros([Ntask, Nwork])
        L1 = np.zeros([parkinglot_size, Nwork])

        sc_index = 0
        sc_crowd_sourcer_index = {}
        for answer in new_active_answers:
            if crowd_sourcer_index[answer.sourcer.id] not in ret:
                continue
            if answer.sourcer.id not in sc_crowd_sourcer_index:
                sc_crowd_sourcer_index[answer.sourcer.id] = sc_index
                sc_index += 1
            if answer.answer == 1:
                L0[question_index[answer.name], sc_crowd_sourcer_index[answer.sourcer.id]] = 1
            else:
                L0[question_index[answer.name], sc_crowd_sourcer_index[answer.sourcer.id]] = -1
            L0[question_size + 2 * parkinglot_index[answer.parkinglot.category.id], sc_crowd_sourcer_index[
                answer.sourcer.id]] = 1  # cred
            L0[question_size + 2 * parkinglot_index[answer.parkinglot.category.id] + 1, sc_crowd_sourcer_index[
                answer.sourcer.id]] = answer.parkinglot.price + 1  # price

            L1[parkinglot_index[answer.parkinglot.category.id]][sc_crowd_sourcer_index[answer.sourcer.id]] = 1
        # L0 置入前做+1处理，防止出现NaN问题(Not a Number) 最后得出的估计值需要相应的-1
        jointMLEGaussian = JointMLEGaussian(L0 + 1, trainset, ans_train, model=3)

        print("mse:", jointMLEGaussian.mseVec)
        return question_size, sc_crowd_sourcer_index, parkinglot_index, jointMLEGaussian
    else:
        return 0, 0, 0, 0



def index(request):
    if request.method == 'GET':

        (question_size, crowd_sourcer_index, parkinglot_index, jointMLEGaussian) = process_answers()
        dict = {}
        jointMLEGaussian.mu -= 1
        if question_size > 0:
            parkinglot_categories = ParkingCategory.objects.all()

            for parkinglot_category in parkinglot_categories:
                parklot = {}
                name = set()
                desc = set()
                min_bid = sys.float_info.max
                parkinglots = ParkingLot.objects.filter(category=parkinglot_category.id).order_by('update_time')
                row_indices, col_indices = np.where(
                    np.round(jointMLEGaussian.nu, 4) == max(np.round(jointMLEGaussian.nu[0], 4))
                )

                best_cred_sourcer_id = []
                for k, v in crowd_sourcer_index.items():
                    if v in col_indices:
                        best_cred_sourcer_id.append(k)

                # print("最好的worker：", best_cred_sourcer_id)

                for parkinglot in parkinglots:
                    if len(name) == 0 and parkinglot.sourcer.id in best_cred_sourcer_id:
                        name = {parkinglot.name}
                    elif parkinglot.sourcer.id in best_cred_sourcer_id:
                        name.add(parkinglot.name)
                    if len(desc) == 0 and parkinglot.sourcer.id in best_cred_sourcer_id:
                        desc = {parkinglot.desc}
                    elif parkinglot.sourcer.id in best_cred_sourcer_id:
                        desc.add(parkinglot.desc)
                    min_bid = min(min_bid, float(parkinglot.bid))

                if parkinglot_category.id in parkinglot_index:
                    parklot["name"] = " ".join(name)
                    parklot["addr"] = parkinglots[0].address
                    parklot["desc"] = " ".join(desc)
                    parklot["bid"] = float(min_bid)
                    parklot["lat"] = float(parkinglot_category.latitude)
                    parklot["lon"] = float(parkinglot_category.longitude)
                    parklot["price"] = round(jointMLEGaussian.mu[question_size + 2 * parkinglot_index[
                        parkinglot_category.id] + 1, 0] - 1 + max(jointMLEGaussian.nu[0]), 1)
                    parklot["cred"] = round(
                        jointMLEGaussian.mu[question_size + 2 * parkinglot_index[parkinglot_category.id], 0], 1)
                    # parklot["create_time"] = parkinglot.create_time
                    # parklot["update_time"] = parkinglot.update_time
                    dict[parkinglot_category.id] = parklot

        return HttpResponse(json.dumps(dict))
    else:
        return HttpResponse("invalid request")


def my(request):
    if request.method == 'GET':

        (question_size, crowd_sourcer_index, parkinglot_index, jointMLEGaussian) = process_answers()
        dict = {}
        if question_size > 0:
            print(jointMLEGaussian.L)
            print(jointMLEGaussian.mu)
            print(jointMLEGaussian.nu)

            parkinglot_categories = ParkingCategory.objects.all()

            for parkinglot_category in parkinglot_categories:
                parklot = {}
                name = ""
                desc = ""
                min_bid = sys.float_info.max
                parkinglots = ParkingLot.objects.filter(category=parkinglot_category.id).order_by('update_time')
                row_indices, col_indices = np.where(
                    np.round(jointMLEGaussian.nu, 4) == max(np.round(jointMLEGaussian.nu[0], 4)))

                best_cred_sourcer_id = []
                for k, v in crowd_sourcer_index.items():
                    if v in col_indices:
                        best_cred_sourcer_id.append(k)

                print(best_cred_sourcer_id)

                for parkinglot in parkinglots:
                    if len(name) == 0 and parkinglot.sourcer.id in best_cred_sourcer_id:
                        name = parkinglot.name
                    elif parkinglot.sourcer.id in best_cred_sourcer_id:
                        name = name + " " + parkinglot.name
                    if len(desc) == 0 and parkinglot.sourcer.id in best_cred_sourcer_id:
                        desc = parkinglot.desc
                    elif parkinglot.sourcer.id in best_cred_sourcer_id:
                        desc = desc + " " + parkinglot.desc
                    min_bid = min(min_bid, float(parkinglot.bid))

                if parkinglot_category.id in parkinglot_index:
                    parklot["name"] = name
                    parklot["addr"] = parkinglots[0].address
                    parklot["desc"] = desc
                    parklot["bid"] = float(min_bid)
                    parklot["lat"] = float(parkinglot_category.latitude)
                    parklot["lon"] = float(parkinglot_category.longitude)
                    parklot["price"] = round(jointMLEGaussian.mu[question_size + 2 * parkinglot_index[
                        parkinglot_category.id] + 1, 0] - 1 + max(jointMLEGaussian.nu[0]), 1)
                    parklot["cred"] = round(
                        jointMLEGaussian.mu[question_size + 2 * parkinglot_index[parkinglot_category.id], 0], 1)
                    # parklot["create_time"] = parkinglot.create_time
                    # parklot["update_time"] = parkinglot.update_time
                    dict[parkinglot_category.id] = parklot

        return HttpResponse(json.dumps(dict))
    else:
        return HttpResponse("invalid request")