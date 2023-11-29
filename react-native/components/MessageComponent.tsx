import { DataFrame } from 'danfojs';
import React, { useEffect } from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';
import BarChart from './graphics/Barchart';
import Table from './graphics/Table';
import PieChart from './graphics/PieChart';
import ScatterChart from './graphics/ScatterChart';
import LineChart from './graphics/LineChart';


type toolMessageProp = {
    graph_type: string,
    explain_criteria: string,
    graph_variable: string,
    data: string,
}
const ToolMessage: React.FC<toolMessageProp> = ({ graph_type, explain_criteria, graph_variable, data }) => {
    if (data === undefined || explain_criteria === undefined || graph_variable === undefined || graph_type === undefined) {
        return <Text>{"Function has been called!\nError... not enough parameters are passed"}</Text>
    }
    const [xaxis, yaxis] = graph_variable.split(",")
    let graph = undefined
    let df = undefined
    try {
        df = new DataFrame(JSON.parse(data))
    } catch (e) {
        graph = <Text>No Data found ...</Text>
    }

    if (df !== undefined) {
        if (!(df.columns.includes(xaxis) && df.columns.includes(yaxis))) {
            graph = <Text>{`ERROR: data do not include the columns of ${xaxis} and ${yaxis}`}</Text>
        }
        if (df.values.length === 0) {
            graph = <Text>No Data found...</Text>
        } else {
            // graph = <Text>{graph_type}</Text>
            switch (graph_type) {
                case "barchart":
                    // console.log(df.column(xaxis).dtype)
                    graph = <BarChart xlabel={xaxis} ylabel={yaxis} title={""}
                        xvals={df.column(xaxis).values as string[]}
                        yvals={df.column(yaxis).values as number[]} />
                    break;
                case "piechart":
                    graph = <PieChart xlabel={xaxis} ylabel={yaxis} title={""}
                        xvals={df.column(xaxis).values as string[]}
                        yvals={df.column(yaxis).values as number[]} />
                    break;
                case "scatterplot":
                    graph = <ScatterChart xlabel={xaxis} ylabel={yaxis} title={""}
                        xvals={df.column(xaxis).values as number[]}
                        yvals={df.column(yaxis).values as number[]} />
                    break;
                case "linechart":
                    graph = <LineChart xlabel={xaxis} ylabel={yaxis} title={""}
                        xvals={df.column(xaxis).values as string[]}
                        yvals={df.column(yaxis).values as number[]} />
                    break
                default:
                    graph = <Table data={df} />
                    break;
            }
        }
    }

    return <View>
        <Text>{`Function has been called!\n`}</Text>
        {graph}
        <Text>{explain_criteria}</Text>
    </View>
}

type messageProp = {
    message: Message
}
const MessageComponent: React.FC<messageProp> = ({ message }) => {
    const isUserMessage = message.role === 'user';
    const isToolNormalMessage = message.role === 'tool' && !message.content.startsWith("ERROR:");
    const messageRoleLabel = isUserMessage ? "You" : "PILLM";
    let data = undefined
    // console.log(message);
    if (isToolNormalMessage) {
        data = JSON.parse(message.content);
    }
    return <View style={[styles.container, isUserMessage ? styles.userContainer : styles.systemContainer]}>
        <View style={styles.messageRole}>
            <Image source={isUserMessage ? require("../resources/user.png") : require("../resources/robot.png")} style={{ width: 24, height: 24 }} />
            <Text style={{ fontWeight: "bold", fontSize: 16 }}>{messageRoleLabel}</Text>
        </View>
        {
            isToolNormalMessage ?
                <ToolMessage graph_type={data['graph_type']} explain_criteria={data['explain_criteria']} graph_variable={data['graph_variable']} data={data['data']} /> : <Text>{message.content}</Text>
        }

    </View>
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
    },
    userContainer: {
        backgroundColor: "#FFF"
    },
    systemContainer: {
        backgroundColor: "#F1F1F1"
    },
    messageRole: {
        flexDirection: "row",
        gap: 6,
        alignItems: "center"
    }

})

export default MessageComponent