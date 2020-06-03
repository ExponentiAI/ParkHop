import numpy as np
from math import exp
import math


class EM_Model:
    def __init__(self, data):
        self.num_data_interval = 50  # 50 data intervals
        self.data_points = data
        self.num_time_interval = self.data_points.shape[1]

    def train_worker_set(self, set_of_workers_id):
        num_workers = len(set_of_workers_id)
        data_points = self.data_points[set_of_workers_id]
        max_value = np.max(data_points)
        min_value = np.min(data_points[data_points > 0])
        interval_range = (max_value - min_value)/self.num_data_interval
        data_interval = [
            min_value+(i+1)*interval_range for i in range(self.num_data_interval)]

        indicator_matrix = np.zeros(
            (self.num_time_interval, num_workers, self.num_data_interval))
        for t in range(self.num_time_interval):
            for k in range(num_workers):
                if data_points[k][t] == 0:
                    break
                for i in range(self.num_data_interval):
                    if data_points[k][t] <= data_interval[i]:
                        indicator_matrix[t][k][i] = 1
                        break
        effort_matrix = np.zeros(
            (num_workers, self.num_data_interval, self.num_data_interval))
        data_prob = np.sum(indicator_matrix, axis=1)/num_workers
        interval_dist = np.zeros(self.num_data_interval)

        th = 1e-10
        iterations = 10  # at least 10 iterations, usually converge after two iterations
        for i in range(iterations):
            old_effort_matrix = effort_matrix.copy()
            old_data_prob = data_prob.copy()
            # E-step
            for k in range(num_workers):
                new_effort_matrix = np.zeros(
                    (self.num_data_interval, self.num_data_interval))
                normalize_vector = np.zeros(self.num_data_interval)
                for t in range(self.num_time_interval):
                    new_effort_matrix = new_effort_matrix + data_prob[t].reshape(
                        self.num_data_interval, 1).dot(indicator_matrix[t][k].reshape(1, self.num_data_interval))
                    normalize_vector = normalize_vector + data_prob[t]

                normalize_vector[normalize_vector == 0] = 1

                effort_matrix[k] = np.divide(
                    new_effort_matrix, normalize_vector.reshape(self.num_data_interval, 1))

            interval_dist = np.sum(data_prob, axis=0)/self.num_time_interval
            # M-step
            for t in range(self.num_time_interval):
                effort_prod_matrix = np.ones(self.num_data_interval)
                for k in range(num_workers):
                    tmp = effort_matrix[k] ** indicator_matrix[t][k]
                    effort_prod_matrix = effort_prod_matrix * \
                        np.product(tmp, axis=1)
                data_prob[t] = interval_dist*effort_prod_matrix
                normalize_value = np.sum(data_prob[t])
                data_prob[t] = data_prob[t]/normalize_value

            # check if converge
            diff1 = np.sum(effort_matrix - old_effort_matrix)

            diff2 = np.sum(data_prob - old_data_prob)
            if diff1 < th and diff2 < th:
                break

        self.reputation_worker_em = np.zeros(num_workers)
        for k in range(num_workers):
            self.reputation_worker_em[k] = np.trace(effort_matrix[k])

        self.estimated_value_em = np.zeros(self.num_time_interval)
        for t in range(self.num_time_interval):
            self.estimated_value_em[t] = min_value + \
                (np.argmax(data_prob[t])+1)*interval_range
