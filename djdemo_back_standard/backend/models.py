from django.db import models

# Create your models here.
import datetime


class Session(models.Model):
    received_msg = models.TextField(null=True, blank=False)
    receive_time = models.DateTimeField('receive_time', null=True, blank=True)
    accepted = models.BooleanField(null=True, blank=False, default=False)

    def __str__(self):
        if self.accepted:
            accepted_str = "ACCEPTED"
        else:
            accepted_str = "REJECTED"

        return str(self.receive_time + "(" + accepted_str + ")" + ": " + self.receive_msg)
