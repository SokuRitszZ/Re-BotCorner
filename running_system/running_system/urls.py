"""running_system URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path

from running_system.views.index import index
from running_system.views.runbot.prepare import prepare
from running_system.views.runbot.run import run
from running_system.views.runbot.start import start
from running_system.views.runbot.compile import compile
from running_system.views.runbot.stop import stop

urlpatterns = [
    path('api/admin/', admin.site.urls),
    path('api/runbot/start/', start),
    path('api/runbot/compile/', compile),
    path('api/runbot/run/', run),
    path('api/runbot/prepare/', prepare),
    path('api/runbot/stop/', stop)
]
