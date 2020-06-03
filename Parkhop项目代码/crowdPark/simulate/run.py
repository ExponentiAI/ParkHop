
import numpy as np
from worker_select import select
from models.em_model import EM_Model
from models.JointMLEGaussian import JointMLEGaussian
from models.unsupervised_model import Unsupervised_Model
from models.wmv_linear_model import Wmv_Linear_Model

def generate_data(num_workers =100,num_plots=100,gt_plot=5,gt_price=10):
    # generate simulation data
    global num_prices 
    num_prices = num_plots
    global num_pois 
    num_pois = num_plots*6
    worker_mu = np.random.normal(0, 1, num_workers)
    worker_var = np.random.lognormal(0, 1, num_workers)

    worker_data = np.zeros((num_workers,num_pois + num_plots+num_prices))
    for i in range(num_workers):
        arr = np.random.normal(1+worker_mu[i],worker_var[i],num_pois)
        worker_data[i,:num_pois] = np.where(arr>0,1,0)

        worker_data[i,num_pois:num_pois+num_plots] = np.rint(np.random.normal(gt_plot+worker_mu[i],worker_var[i],num_plots))
        worker_data[i,num_pois+num_plots:] = np.rint(np.random.normal(gt_price+worker_mu[i],worker_var[i],num_prices))

    worker_ids = [i for i in range(num_workers)]
    return worker_data,worker_ids,worker_var


def getMean(mu):
    pio = np.mean(mu[:num_pois])
    plot = np.mean(mu[num_pois:num_pois+num_plots])
    price = np.mean(mu[num_pois+num_plots:num_pois+num_plots+num_prices])
    print((pio,plot,price))
    return (pio,plot,price)

def test(worker_data,worker_ids,worker_var):
    
    true_worker_var = worker_var
    true_reputation = 1/true_worker_var
    e = true_reputation/np.sum(true_reputation)
    
    print("  --un--  ")
    
    # unsupervised model
    unsupervised_model = Unsupervised_Model(worker_data)
    unsupervised_model.train_worker_set(np.array(worker_ids))
    uns_mu = unsupervised_model.estimated_value_unsupervised
    un_mu_me = getMean(uns_mu)
    
    print("  --joint--  ")
    # 联合估计
    L = worker_data.T

    trainset=np.arange(0,num_pois) #控制项目（已知真实值）
    ans_train=np.ones((1,num_pois)) #控制项目真实值（已知真实值）

    joint = JointMLEGaussian(L, trainset, ans_train, model=3)
    jmu = joint.mu.T[0]
    jmu_me = getMean(jmu)
    
    print("  --WMV--  ")
    # WMV
    wmv = Wmv_Linear_Model(worker_data.T,trainset,ans_train,[0,1])
    wmv_mu = np.hstack([np.ones((num_pois)),wmv.mu])
    wmv_mu = getMean(wmv_mu)
    
    print("  --EM--  ")
    # EM model
    em_model = EM_Model(worker_data)
    em_model.train_worker_set(np.array(worker_ids))
    em_mu = em_model.estimated_value_em
    em_mu_me = getMean(em_mu)
    
    return un_mu_me,jmu_me,wmv_mu,em_mu_me


if __name__ == '__main__':

    global num_plots 
    global num_workers
    num_workers =100
    num_plots = 100
    truth_plots = 6
    truth_price = 8
    worker_data,worker_ids,worker_var = generate_data(num_workers,num_plots,truth_plots,truth_price)
    print(f"ground truth:(1,{truth_plots},{truth_price})")

    test(worker_data,worker_ids,worker_var)
    
    print("-*"*10)
    reliabilities = np.zeros((num_workers))
    for i in range(num_workers):
        reliabilities[i] = len([j for j in worker_data[i,:num_pois] if j >0])
    workers = select(reliabilities,num_workers,num_pois,2)
    print(f"the number of workers selected：{len(workers)}")
    print("-*"*10)
    
    selected_worker_data = worker_data[workers]
    selected_worder_var = worker_var[workers]
    selected_worker_id = [i for i in range(len(workers))]
    test(selected_worker_data,selected_worker_id,selected_worder_var)