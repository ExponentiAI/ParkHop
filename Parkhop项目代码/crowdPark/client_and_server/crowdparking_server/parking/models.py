from django.db import models
import datetime


# Create your models here.
class CrowdSourcer(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField("sourcer_name", max_length=30)
    password = models.CharField("sourcer_pass", max_length=30)
    session_key = models.CharField("sourcer_session", max_length=30)
    create_time = models.DateTimeField(default=datetime.datetime.now)
    update_time = models.DateTimeField(default=datetime.datetime.now)


class ParkingCategory(models.Model):
    id = models.AutoField(primary_key=True)
    latitude = models.DecimalField("category_latitude", max_digits=12, decimal_places=6, default=00.00)
    longitude = models.DecimalField("category_longitude", max_digits=12, decimal_places=6, default=00.00)
    cred = models.DecimalField("credibility", max_digits=6, decimal_places=4, default=00.0)
    price = models.DecimalField("parkinglot_price", max_digits=12, decimal_places=6, default=00.00)


class ParkingLot(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField("parkinglot_name", max_length=30)
    desc = models.CharField("description", max_length=100)
    address = models.CharField("address", max_length=100)
    sourcer = models.ForeignKey(CrowdSourcer, on_delete=models.CASCADE, related_name="crowdsourcer_id")
    bid = models.DecimalField("min_bid", max_digits=5, decimal_places=2)
    latitude = models.DecimalField("latitude", max_digits=12, decimal_places=6, default=00.00)
    longitude = models.DecimalField("longitude", max_digits=12, decimal_places=6, default=00.00)
    create_time = models.DateTimeField(default=datetime.datetime.now)
    update_time = models.DateTimeField(default=datetime.datetime.now)
    # idle_num = models.IntegerField("parkinglot_idle_num", default=0)
    price = models.DecimalField("parkinglot_price", max_digits=12, decimal_places=6, default=00.00)
    category = models.ForeignKey(ParkingCategory, on_delete=models.CASCADE, related_name="parking_category")


class Answer(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField("answer_question_name", max_length=30)  # poi name string
    parkinglot = models.ForeignKey(ParkingLot, on_delete=models.CASCADE, related_name="answer_parkinglot_id")
    sourcer = models.ForeignKey(CrowdSourcer, on_delete=models.CASCADE, related_name="answer_crowdsourcer_id")
    answer = models.BooleanField("answer_correctness")
    create_time = models.DateTimeField(default=datetime.datetime.now)
    update_time = models.DateTimeField(default=datetime.datetime.now)


class Driver(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField("driver_name", max_length=30)
    password = models.CharField("driver_pass", max_length=30)
    session_key = models.CharField("driver_session", max_length=30)
    create_time = models.DateTimeField(default=datetime.datetime.now)
    update_time = models.DateTimeField(default=datetime.datetime.now)


class Bid(models.Model):
    id = models.AutoField(primary_key=True)
    bid = models.DecimalField("bid", max_digits=5, decimal_places=2)
    parkinglot = models.ForeignKey(ParkingCategory, on_delete=models.CASCADE, related_name="parkinglot_category_id")
    driver = models.ForeignKey(Driver, on_delete=models.CASCADE, related_name="driver_id")
    create_time = models.DateTimeField(default=datetime.datetime.now)
    update_time = models.DateTimeField(default=datetime.datetime.now)
