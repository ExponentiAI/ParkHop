from django.contrib import admin

# Register your models here.
from .models import *

admin.site.register(CrowdSourcer)
admin.site.register(ParkingLot)
admin.site.register(Driver)
admin.site.register(Bid)
admin.site.register(Answer)
admin.site.register(ParkingCategory)