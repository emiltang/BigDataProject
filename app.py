from io import StringIO
from threading import Thread
from altair import Chart, condition, value, datum, LookupData,topo_feature, layer, sphere, graticule
from flask import Flask
from pandas import read_csv, DataFrame
from vega_datasets import data
from kafka import KafkaConsumer


DATA_FILE = '/user/hadoop/output/lang_results/part-00000-ded225ba-fd59-426b-886e-2ddaa37c1de6-c000.csv'

DATA = dict()

app = Flask(__name__)

STATES = {
    "Alabama": "1",
    "Alaska": "2",
    "Arizona": "4",
    "Arkansas": "5",
    "California": "6",
    "Colorado": "8",
    "Connecticut": "9",
    "Delaware": "10",
    "Florida": "12",
    "Georgia": "13",
    "Hawaii": "15",
    "Idaho": "16",
    "Illinois": "17",
    "Indiana": "18",
    "Iowa": "19",
    "Kansas": "20",
    "Kentucky": "21",
    "Louisiana": "22",
    "Maine": "23",
    "Maryland": "24",
    "Massachusetts": "25",
    "Michigan": "26",
    "Minnesota": "27",
    "Mississippi": "28",
    "Missouri": "29",
    "Montana": "30",
    "Nebraska": "31",
    "Nevada": "32",
    "New Hampshire": "33",
    "New Jersey": "34",
    "New Mexico": "35",
    "New York": "36",
    "North Carolina": "37",
    "North Dakota": "38",
    "Ohio": "39",
    "Oklahoma": "40",
    "Oregon": "41",
    "Pennsylvania": "42",
    "Rhode Island": "44",
    "South Carolina": "45",
    "South Dakota": "46",
    "Tennessee": "47",
    "Texas": "48",
    "Utah": "49",
    "Vermont": "50",
    "Virginia": "51",
    "Washington": "53",
    "West Virginia": "54",
    "Wisconsin": "55",
    "Wyoming": "56",
    "American Samoa": "60",
    "Guam": "66",
    "Northern Mariana Islands": "69",
    "Puerto Rico": "72",
    "Virgin Islands": "78",
}


@app.route('/')
def hello_world():
    #with pydoop.hdfs.open(DATA_FILE) as data_file:
       # file_buffer = StringIO()

        # not sure if pandas dataframe is necessary
        #dataframe = read_csv(data_file)
        #chart = Chart(dataframe).mark_bar().encode(
         #   x='lang:O',
         #   y="count:Q",
        #).properties(width=600)

        #chart.save(file_buffer, format='html')

    return "Hello World"


@app.route('/choropleth')
def choropleth_map():

    dataframe = DataFrame(DATA.items(), columns=['state', 'count'])

    file_buffer = StringIO()

    # not sure if pandas dataframe is necessary
    #dataframe = read_csv(data_file)

    # Data generators for the background
    sphere = sphere()
    graticule = graticule()

    # Source of land data
    source = topo_feature(data.world_110m.url, 'countries')

    # Layering and configuring the components
    layer(
        Chart(sphere).mark_geoshape(fill='lightblue'),
        Chart(graticule).mark_geoshape(
            stroke='white', strokeWidth=0.5),
        # .transform_lookup(lookup = "id", from_=altair.LookupData(dataframe, 'x', ['count']))
        Chart(source).mark_geoshape(
            fill='ForestGreen', stroke='black')
    ).project(
        'naturalEarth1'
    ).properties(width=600, height=400).configure_view(stroke=None)

    chart = Chart(dataframe).mark_bar().encode(
        x='lang:O',
        y="count:Q",
    ).properties(width=600)

    chart.save(file_buffer, format='html')

    return file_buffer.getvalue()


def map_state_to_fips_id(row):
    return STATES.get(row['state'])


@app.route('/map')
def map():

    dataframe = DataFrame(DATA.items(), columns=['state', 'count'])

    if len(dataframe) == 0:
        return f"No data {len(DATA)}"

    dataframe['id'] = dataframe.apply(map_state_to_fips_id, axis=1)
    dataframe = dataframe.dropna()
    print(dataframe)

    states = topo_feature(data.us_10m.url, 'states')

    chart = Chart(states).mark_geoshape().encode(
        color='count:Q'
    ).transform_lookup(
        lookup='id',
        from_=LookupData(dataframe, 'id', ['count'])
    ).project(
        type='albersUsa'
    ).properties(
        width=500,
        height=300
    )

    file_buffer = StringIO()

    chart.save(file_buffer, format='html')

    return file_buffer.getvalue()


def kafka_listener(sink):

    kafka = KafkaConsumer(
        'counts',
        bootstrap_servers=['localhost:9092'],
        key_deserializer=lambda x: x.decode("utf-8"),
        value_deserializer=lambda x: int(x)
    )

    for message in kafka:
        print(message)
        sink[message.key] = message.value


Thread(target=kafka_listener, args=(DATA,)).start()
