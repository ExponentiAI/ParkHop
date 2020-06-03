"""crowdparking URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.8/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Add an import:  from blog import urls as blog_urls
    2. Add a URL to urlpatterns:  url(r'^blog/', include(blog_urls))
"""
from django.conf.urls import url
from . import views
from django.conf import settings

urlpatterns = [
    url('login/', views.user_login, name='login'),
    url('report/', views.report_parkinglot, name='report'),
    url('bid/', views.bid_parkinglot, name='bid'),
    url('(?P<parkinglot_id>[0-9]+)/cancel_bid/', views.cancel_parkinglotbid, name='cancel_bid'),
    url('(?P<parkinglot_id>[0-9]+)/bids/', views.parkinglot_bids, name='bids'),
    url('demo/', views.demo, name='demo'),
    url('get_data/', views.getData, name='get_data')
]

if settings.DEBUG is False:  # if DEBUG is True it will be served automatically
    urlpatterns += patterns('',
                            url(r'^static/(?P.*)$', 'django.views.static.serve',
                                {'document_root': settings.STATIC_ROOT}),
                            )
