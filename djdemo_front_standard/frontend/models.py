from django.db import models
import datetime


class Session(models.Model):
    received_msg = models.TextField(null=True, blank=False)
    receive_time = models.DateTimeField('receive_time', null=True, blank=True)
    sent_msg = models.TextField(null=True, blank=True)
    send_time = models.DateTimeField('send_time', null=True, blank=True)

    def __str__(self):
        return str(self.receive_time + ": " + self.received_msg[0: min(16, len(self.received_msg))])


class RelayMode(models.Model):
    mode = models.CharField(max_length=64, null=True, blank=False,
                            choices=[('PassThrough', 'PassThrough'), ('Insert', 'Insert'),
                                     ('Modify', 'Modify')], default='Modify')

    def __str__(self):
        return self.mode

    def save(self, *args, **kwargs):
        if self.__class__.objects.count():
            self.pk = self.__class__.objects.first().pk
        super().save(*args, **kwargs)
