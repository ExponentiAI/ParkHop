
import sys
import time
import numpy as np
from numpy import amax, tile, transpose, mean, dot


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
        self.testdx = np.array(self.setdiff1d(
            np.arange(1, self.Ntask + 1), self.traindx), dtype=int)
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
        self.update_w_mu = False
        self.update_w_nu = False
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
        self.mseVec = np.zeros(self.maxIter, dtype=float)  # one dim
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
                self.mu[self.testdx - 1] = (self.w_mu[self.testdx - 1] * (((self.L[self.testdx - 1, ] - np.ones(
                    [self.testdx.size, 1]) * self.nu) * self.A[self.testdx - 1, ]).dot(self.w_nu.transpose()))
                    + self.mu_prior[self.testdx - 1] * self.w_mu_prior[self.testdx - 1]) / ((
                        self.w_mu[
                            self.testdx - 1] * (
                            self.A[
                                self.testdx - 1, ].dot(
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
                self.mseVec[iter] = ((self.mu - self.ptrue)
                                     ** 2).mean(dtype=float)

            self.cal_posterior_log_likelihood()

            self.posteriorVec[iter] = self.loglikelihood + self.logprior

            # print(self.loglikelihood + self.logprior)

            # calculate posterior log likelihood

            # check convergence error
            eps = 2.2204e-16
            err = max([max(abs(self.old_mu - self.mu)) / (max(abs(self.mu)) + eps),
                       max(max(abs(self.old_nu - self.nu))) /
                       (max(max(abs(self.nu))) + eps),
                       max(abs(self.old_w_mu - self.w_mu)) /
                       (max(abs(self.w_mu)) + eps),
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
