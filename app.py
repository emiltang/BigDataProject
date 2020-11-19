from io import StringIO

import pydoop.hdfs
from altair import Chart, condition, value, datum
from flask import Flask
from pandas import read_csv

DATA_FILE = '/user/hadoop/output/lang_results/part-00000-ded225ba-fd59-426b-886e-2ddaa37c1de6-c000.csv'

app = Flask(__name__)


@app.route('/')
def hello_world():
    with pydoop.hdfs.open(DATA_FILE) as data_file:
        file_buffer = StringIO()

        # not sure if pandas dataframe is necessary
        dataframe = read_csv(data_file)
        chart = Chart(dataframe).mark_bar().encode(
            x='lang:O',
            y="count:Q",
        ).properties(width=600)

        chart.save(file_buffer, format='html')
    
        return file_buffer.getvalue()
