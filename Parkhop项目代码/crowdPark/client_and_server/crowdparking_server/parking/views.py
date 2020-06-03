import json
import random
import string
import time

import requests as rq
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from random import randint
import numpy as np

from .models import *


# Create your views here.

@csrf_exempt
def user_login(request):
    result = {}
    try:
        if request.method == 'POST':
            username = request.POST['username']
            password = request.POST['password']
            user_type = request.POST['user_type']
            if int(user_type) == 0:  # sourcer
                sourcer = CrowdSourcer.objects.get(name=username, password=password)
                if sourcer:
                    session_key = ''.join(random.sample(string.ascii_letters + string.digits, 8))
                    sourcer.session_key = session_key
                    sourcer.save()
                    result["user_id"] = sourcer.id
                    result["user_type"] = 0
                    result["session_key"] = session_key
            if int(user_type) == 1:
                driver = Driver.objects.get(name=username, password=password)
                if driver:
                    session_key = ''.join(random.sample(string.ascii_letters + string.digits, 8))
                    driver.session_key = session_key
                    driver.save()
                    result["user_id"] = driver.id
                    result["user_type"] = 1
                    result["session_key"] = session_key

            return HttpResponse(json.dumps(result))
        else:
            return HttpResponse(result)
    except:
        return HttpResponse(result)


@csrf_exempt
def report_parkinglot(request):
    result = 0
    try:
        if request.method == 'POST':
            sourcer_id = request.POST['sourcer_id']
            latitude = request.POST['lat']
            longitude = request.POST['lon']
            name = request.POST['name']
            desc = request.POST['desc']
            addr = request.POST['addr']
            bid = float(request.POST['bid'])
            price = float(request.POST['price'])
            session_key = request.POST['session_key']
            sourcer = CrowdSourcer.objects.get(id=sourcer_id, session_key=session_key)

            # find the right category
            in_category = False
            parking_categories = ParkingCategory.objects.all()
            for parking_category in parking_categories:
                if in_category:
                    break
                else:
                    lat_diff = abs(float(parking_category.latitude) - float(latitude))
                    lon_diff = abs(float(parking_category.longitude) - float(longitude))
                    if lat_diff < 0.001 and lon_diff < 0.001:
                        in_category = True
                        parkinglot_in_category_count = len(ParkingLot.objects.filter(category=parking_category.id))
                        parking_category.latitude = (parkinglot_in_category_count * float(
                            parking_category.latitude) + float(latitude)) / (1 + parkinglot_in_category_count)
                        parking_category.longitude = (parkinglot_in_category_count * float(
                            parking_category.longitude) + float(longitude)) / (1 + parkinglot_in_category_count)
                        parking_category.save()
                        dup_parkinglots = ParkingLot.objects.filter(category=parking_category.id, sourcer=sourcer_id)
                        if len(dup_parkinglots) > 0:  # has duplicate, update
                            for dup_parkinglot in dup_parkinglots:
                                dup_parkinglot.delete()

                        parkinglot = ParkingLot(name=name, desc=desc, address=addr, sourcer=sourcer, bid=bid,
                                                latitude=latitude, longitude=longitude, price=price,
                                                category=parking_category)
                        parkinglot.save()

            # if not exist, add a new category
            if not in_category:
                new_parking_category = ParkingCategory(latitude=latitude, longitude=longitude)
                new_parking_category.save()
                parkinglot = ParkingLot(name=name, desc=desc, address=addr, sourcer=sourcer, bid=bid, latitude=latitude,
                                        longitude=longitude, price=price, category=new_parking_category)
                parkinglot.save()

            answers = request.POST['answer'].split(',')
            for answer in answers:
                answer_name = answer.split('=')[0]
                answer_score = int(answer.split('=')[1])
                new_answer = Answer(name=answer_name, sourcer=sourcer, parkinglot=parkinglot, answer=answer_score)
                new_answer.save()
            result = 1
            return HttpResponse(result)
        else:
            return HttpResponse(result)
    except:
        return HttpResponse(result)


@csrf_exempt
def bid_parkinglot(request):
    result = 0
    try:
        if request.method == 'POST':
            parking_id = request.POST['parking_id']
            driver_id = request.POST['driver_id']
            session_key = request.POST['session_key']
            bid = float(request.POST['bid'])
            parking_category = ParkingCategory.objects.get(id=parking_id)
            driver = Driver.objects.get(id=driver_id, session_key=session_key)
            old_bids = Bid.objects.filter(parkinglot=parking_id, driver=driver_id)
            if len(old_bids) > 0:
                old_bids[0].bid = bid
                old_bids[0].save()
            else:
                new_bid = Bid(bid=bid, parkinglot=parking_category, driver=driver)
                new_bid.save()
            result = 1
            return HttpResponse(result)
        else:
            return HttpResponse(result)
    except:
        return HttpResponse(result)


@csrf_exempt
def cancel_parkinglotbid(request, parkinglot_id):
    result = 0
    try:
        if request.method == 'POST':
            driver_id = request.POST['driver_id']
            session_key = request.POST['session_key']
            driver = Driver.objects.get(id=driver_id, session_key=session_key)
            old_bids = Bid.objects.filter(parkinglot=parkinglot_id, driver=driver_id)
            if len(old_bids) > 0:
                old_bids[0].delete()
                result = 1
                return HttpResponse(result)
        return HttpResponse(result)
    except:
        return HttpResponse(result)


def parkinglot_bids(request, parkinglot_id):
    if request.method == 'GET':
        bids = Bid.objects.filter(parkinglot=parkinglot_id).order_by('bid')
        dict = {}
        for bid in bids:
            cbid = {}
            cbid["user_id"] = bid.driver.id
            cbid["bid"] = float(bid.bid)
            cbid["time"] = epoch = int(time.mktime(bid.update_time.timetuple()) * 1000)
            dict[bid.id] = cbid
        return HttpResponse(json.dumps(dict))
    else:
        return HttpResponse("invalid request")


# 百度地图请求地址
url = 'http://api.map.baidu.com/place/v2/search'
# 请求的key
ak = '8y8E0LGxz8kWGl9TVaPpW0tgMqVhinVc'


def demo(request):
    """
    演示数据生成（基于百度地图的真实地理位置信息）
    """
    # 当前已有的众包人员
    workers = CrowdSourcer.objects.all()

    # 少于30人时，生成众包人员
    faker = Faker()
    max_worker_num = 50
    if len(workers) < max_worker_num:
        for c in range(max_worker_num - len(workers)):
            cr = CrowdSourcer()
            cr.name = faker.name()
            cr.password = faker.password(length=8, special_chars=False, digits=True, )
            cr.session_key = faker.pystr(min_chars=8, max_chars=12)
            cr.create_time = faker.date_time_between(start_date="-30d", end_date="-1d", tzinfo=None)
            cr.update_time = cr.create_time
            cr.save()
    poi_size  = 6
    pre_correct_nums = np.round(np.random.beta(3, 2, max_worker_num) * poi_size)

    # 区域中心点 用来找停车位的
    plot_cats = [
        '28.201066, 112.954793',  # 麓山才苑
         '28.188857, 112.948774',  # 湖南师大
         '28.179172, 112.946743',  # 湖大
         '28.173161, 112.938675',  # 中南
        '28.191919, 112.970815',  # 温沙城堡中西餐厅
        '28.191867, 112.974960',  # 温莎KTV
        '28.195492, 112.976462',  # 五一广场
        '28.202774, 112.975451',  # 潮宗古街
        '28.209524, 112.979704',  # 星沙农村商业银行（湘春路支行）
        '28.222981, 112.982684',  # 华伦门照相馆
    ]

    radius = 400
    location = '28.179,112.943'
    keyword = '停车场'
    page_num = 0
    page_size = 8
    params = {'query': keyword, 'location': location, 'radius': radius,
              'page_num': page_num, 'page_size': page_size,
              'output': 'json', 'ak': ak}

    # 以plot_cats中的坐标为区域中心点，方圆400m为半径，取3-8个停车场，作为所有工作者要提交的停车位信息，同时搜索这个停车位方圆400m的POI地点（8-14个），作为测试问题。
    # 即：
    # for 中心点 in plot_cats：
    #     找到中心点附近的 3-8 个停车位 plots
    #     for worker in 工作者：
    #         for 停车位plot in plots：
    #             worker提交这个plot的停车位信息，同时：
    #                 找到停车位plot附近的8-14个POI地点，供worker测试
    bindex = 0
    for category in plot_cats:
        # 防被ban
        bindex += 1
        if bindex != 1 and bindex % 2 == 1:
            time.sleep(60)

        params['location'] = category
        res = rq.get(url, params=params)
        lot_array = res.json()['results']
        category_size = len(lot_array)
        if category_size == 0:
            continue
        min_cats = 6
        max_cats = category_size
        if category_size <= min_cats:
            min_cats = category_size
        #  所有工作者都提交同样数量的目标项答案
        plots_size = random.randint(min_cats, max_cats)
        for worker in workers:
            # plots_size = random.randint(min_cats, max_cats)
            for i in range(plots_size):
                sourcer_id = worker.id
                # 停车位信息
                lot = lot_array[i]
                latitude = lot['location']['lat']
                longitude = lot['location']['lng']
                name = lot['name']
                addr = lot['address']
                desc = addr
                # 竞价默认随机1-4
                bid = float(random.randint(1, 4))
                # 停车费随机5-18
                price = float(random.randint(5, 18))

                # find the right category 将距离相近的停车位归为一个看待
                in_category = False
                parking_categories = ParkingCategory.objects.all()
                for parking_category in parking_categories:
                    if in_category:
                        break
                    else:
                        lat_diff = abs(float(parking_category.latitude) - float(latitude))
                        lon_diff = abs(float(parking_category.longitude) - float(longitude))
                        # 经纬度差0.001即视为同一个地点
                        if lat_diff < 0.001 and lon_diff < 0.001:
                            in_category = True
                            parkinglot_in_category_count = len(ParkingLot.objects.filter(category=parking_category.id))
                            parking_category.latitude = (parkinglot_in_category_count * float(
                                parking_category.latitude) + float(latitude)) / (1 + parkinglot_in_category_count)
                            parking_category.longitude = (parkinglot_in_category_count * float(
                                parking_category.longitude) + float(longitude)) / (1 + parkinglot_in_category_count)
                            parking_category.save()
                            dup_parkinglots = ParkingLot.objects.filter(category=parking_category.id,
                                                                        sourcer=sourcer_id)
                            if len(dup_parkinglots) > 0:  # has duplicate, update
                                for dup_parkinglot in dup_parkinglots:
                                    dup_parkinglot.delete()

                            parkinglot = ParkingLot(name=name, desc=desc, address=addr, sourcer=worker, bid=bid,
                                                    latitude=latitude, longitude=longitude, price=price,
                                                    category=parking_category)
                            parkinglot.save()

                # if not exist, add a new category
                if not in_category:
                    new_parking_category = ParkingCategory(latitude=latitude, longitude=longitude)
                    new_parking_category.save()
                    parkinglot = ParkingLot(name=name, desc=desc, address=addr, sourcer=worker, bid=bid,
                                            latitude=latitude, longitude=longitude, price=price,
                                            category=new_parking_category)
                    parkinglot.save()

                # POI 问题

                randomPool = np.zeros(poi_size)

                answers = getPOIs(location=str(latitude) + ',' + str(longitude), pois_size=poi_size)
                for indx,answer in enumerate(answers):
                    answer_name = answer['name']
                    print("poi:", answer_name)
                    answer_score = randomPool[indx]
                    new_answer = Answer(name=answer_name, sourcer=worker, parkinglot=parkinglot, answer=answer_score)
                    new_answer.save()

    return HttpResponse("Demo")


def getPOIs(location, pois_size):
    """
    获取附近的POI点
    :param location: 中心点坐标
    :param pois_size: 一次获取的POI个数
    :return: POI 数据集
    """
    keyword = '政府机构$美食$酒店$房地产$医疗$公司$运动健身$文化传媒'
    radius = 400
    page_num = 0
    page_size = pois_size
    params = {'query': keyword, 'location': location, 'radius': radius,
              'page_num': page_num, 'page_size': page_size,
              'output': 'json', 'ak': ak}
    res = rq.get(url, params=params)
    time.sleep(1)
    return res.json()['results']

def getData(request):
    # 区域中心点 用来找停车位的
    plot_cats = [
        # '28.201066, 112.954793',  # 麓山才苑
        '28.188857, 112.948774',  # 湖南师大
        '28.179172, 112.946743',  # 湖大
        '28.173161, 112.938675',  # 中南
        '28.191919, 112.970815',  # 温沙城堡中西餐厅
        '28.191867, 112.974960',  # 温莎KTV
        '28.195492, 112.976462',  # 五一广场
        '28.202774, 112.975451',  # 潮宗古街
        '28.209524, 112.979704',  # 星沙农村商业银行（湘春路支行）
        '28.222981, 112.982684',  # 华伦门照相馆
    ]

    radius = 400
    location = '28.179,112.943'
    keyword = '停车场'
    page_num = 0
    page_size = 8
    params = {'query': keyword, 'location': location, 'radius': radius,
              'page_num': page_num, 'page_size': page_size,
              'output': 'json', 'ak': ak}
    bindex = 0
    plot_dir = "/Users/xio/Dev/Xiu/Crowdparking/crowd_data/plots/"
    pois_dir = "/Users/xio/Dev/Xiu/Crowdparking/crowd_data/pois/"
    for category in plot_cats:
        # 防被ban
        bindex += 1
        if bindex != 1 and bindex % 2 == 1:
            time.sleep(60)

        params['location'] = category
        res = rq.get(url, params=params)
        lot_array = res.json()['results']
        with open(plot_dir+category+'_plot.json', 'w', encoding='utf-8') as outfile:
            json.dump(lot_array, outfile, ensure_ascii=False)
        plots_size = len(lot_array)
        for i in range(plots_size):
            # 停车位信息
            lot = lot_array[i]
            latitude = lot['location']['lat']
            longitude = lot['location']['lng']
            name = lot['name']
            addr = lot['address']
            desc = addr
            loc = str(latitude) + ',' + str(longitude)
            answers = getPOIs(location=loc, pois_size=20)
            with open(pois_dir+loc + '_pois.json', 'w', encoding='utf-8') as outfile:
                json.dump(answers, outfile, ensure_ascii=False)
    return HttpResponse("Finished")
