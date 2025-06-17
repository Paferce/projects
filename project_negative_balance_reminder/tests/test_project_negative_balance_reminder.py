##############################################################################
# For copyright and license notices, see __manifest__.py file in root directory
##############################################################################
from odoo import fields
from odoo.tests import TransactionCase


class TestProjectNegativeBalanceReminder(TransactionCase):
    def setUp(self):
        super().setUp()
        self.project = self.env['project.project'].create({
            'name': 'Test Project',
            'balance': -100,
        })

    def test_negative_balance_notification(self):
        self.project.check_and_notify_negative_balance()
        self.assertEqual(
            self.project.last_notification_date,
            fields.Date.today(),
            'Last notification date was not updated correctly'
        )
