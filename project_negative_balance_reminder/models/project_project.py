###############################################################################
# For copyright and license notices, see __manifest__.py file in root directory
###############################################################################
from odoo import fields, models


class ProjectProject(models.Model):
    _inherit = 'project.project'

    balance = fields.Float(
        string='Financial Balance',
        help='Current financial balance of the project',
    )

    last_notification_date = fields.Date(
        string='Last notification date',
        help='Date of the last notification sent for negative balance',
        readonly=True,
        copy=False,
    )
    renewal_period_days = fields.Integer(
        string='Renewal period (days)',
        help='Number of days to wait before sending another notification',
        default=30,
    )

    def check_and_notify_negative_balance(self):
        today = fields.Date.today()
        for project in self.search([]):
            if (
                project._has_negative_balance()
                and project._should_notify(today)
            ):
                project._send_negative_balance_email()
                project.last_notification_date = today

    def _has_negative_balance(self):
        return self.balance < 0

    def _should_notify(self, today):
        return (
            not self.last_notification_date
            or (today - self.last_notification_date).days
            >= self.renewal_period_days
        )

    def _send_negative_balance_email(self):
        followers = self.message_follower_ids.mapped('partner_id.email')
        if followers:
            mail_template = self.env.ref(
                'project_negative_balance_reminder.'
                'email_template_negative_balance'
            )
            if mail_template:
                mail_template.send_mail(self.id, force_send=True)
