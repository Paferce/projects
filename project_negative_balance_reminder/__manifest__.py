{
    'name': 'Negative Balance Reminder',
    'version': '16.0.1.0.0',
    'category': 'Tools',
    'summary': 'Followers are notified when balance is negative.',
    'author': 'Trey, Kilobytes de Soluciones',
    'website': 'https://www.trey.es',
    'license': 'AGPL-3',
    'depends': [
        'base',
        'project',
        'mail'
    ],
    'data': [
        'data/email_template.xml',
        'data/ir_cron_data.xml',
        'views/project_views.xml',
    ],
    'images': [
        #'static/description/banner.png',
    ],
}