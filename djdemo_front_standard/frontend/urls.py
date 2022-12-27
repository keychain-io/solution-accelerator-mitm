from django.urls import path
from django.conf import settings
from . import views
import sys

# Start the Monitor if we are running the server and using Keychain
if getattr(settings, "WITH_KEYCHAIN", False):
    try:
        command = sys.argv[1]
    except IndexError:
        command = ""
    if command == "runserver":
        from . import start_monitoring

urlpatterns = [
    path(r'recv', views.recv, name='recv')
]
