from django.contrib import admin

# Register your models here.

from .models import Session, RelayMode

admin.site.register(Session)
admin.site.register(RelayMode)
