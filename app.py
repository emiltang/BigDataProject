from io import StringIO

import pydoop.hdfs
import altair
from altair import Chart, condition, value, datum
from flask import Flask
from pandas import read_csv
from vega_datasets import data

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

@app.route('/choropleth')
def choropleth_map():
    with pydoop.hdfs.open(DATA_FILE) as data_file:
        file_buffer = StringIO()

        # not sure if pandas dataframe is necessary
        dataframe = read_csv(data_file)

        # Data generators for the background
        sphere = alt.sphere()
        graticule = alt.graticule()

        # Source of land data
        source = alt.topo_feature(data.world_110m.url, 'countries')

        # Layering and configuring the components
        altair.layer(
            altair.Chart(sphere).mark_geoshape(fill='lightblue'),
            altair.Chart(graticule).mark_geoshape(stroke='white', strokeWidth=0.5),
            altair.Chart(source).mark_geoshape(fill='ForestGreen', stroke='black') #.transform_lookup(lookup = "id", from_=altair.LookupData(dataframe, 'x', ['count']))
        ).project(
            'naturalEarth1'
        ).properties(width=600, height=400).configure_view(stroke=None)

        chart = Chart(dataframe).mark_bar().encode(
            x='lang:O',
            y="count:Q",
        ).properties(width=600)

        chart.save(file_buffer, format='html')
    
        return file_buffer.getvalue()