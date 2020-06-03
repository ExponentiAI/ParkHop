import numpy as np
from math import exp
import math


class Unsupervised_Model:
    def __init__(self, data):
        self.data_points = data
        self.num_time_intervals = self.data_points.shape[1]

    def train_worker_set(self, set_of_workers_id):
        num_workers = len(set_of_workers_id)
        if num_workers == 1:
            self.reputation_worker_unsupervised = [1]
            return
        sample_data = self.data_points[set_of_workers_id].T
        self.estimated_value_unsupervised = np.zeros(self.num_time_intervals)

        quality_worker_init = np.ones(num_workers)
        quality_worker = np.ones((self.num_time_intervals, num_workers))

        quality_worker_accu = np.zeros((self.num_time_intervals, num_workers))
        exp_decay = 0.5

        epsilon = 1e-3

        w = sample_data[0].dot(quality_worker_init)/np.sum(quality_worker_init)

        dist = (sample_data[0] - w)**2

        norm_dist = np.sum(dist)
        if norm_dist == 0:
            quality_worker[0][:] = 1
        else:
            inv_dist = 1/(dist/norm_dist + epsilon)
            quality_worker[0] = inv_dist/np.sum(inv_dist)

        quality_worker_accu[0] = quality_worker[0]
        self.estimated_value_unsupervised[0] = w

        for t in range(1, self.num_time_intervals):
            active_worker = sample_data[t] > 0
            if np.sum(active_worker) == 0:
                continue
            w = sample_data[t][active_worker].dot(
                quality_worker[t-1][active_worker])/np.sum(quality_worker[t-1][active_worker])
            self.estimated_value_unsupervised[t] = w
            dist = (sample_data[t][active_worker] - w)**2
            norm_dist = np.sum(dist)
            if norm_dist == 0:
                quality_worker[t] = quality_worker[t-1]
            else:
                inv_dist = 1/(dist/norm_dist + epsilon)

                quality_worker[t] = quality_worker[t-1]
                quality_worker[t][active_worker] = inv_dist/np.sum(inv_dist)
            quality_worker_accu[t] = quality_worker_accu[t-1] * \
                exp_decay + quality_worker[t]
            self.reputation_worker_unsupervised = 1 / \
                (1 + np.exp(-(quality_worker_accu[t]-1)))
