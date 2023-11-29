import { DataFrame } from "danfojs";
import React from "react";
import { FlatList, ListRenderItem, StyleSheet, Text, View } from "react-native";


const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection: 'row',
        padding: 16,
    },
    header: {
        flexDirection: 'row',
        marginBottom: 8,
    },
    headerText: {
        fontWeight: 'bold',
        marginRight: 16,
    },
    row: {
        flexDirection: 'row',
        marginBottom: 8,
    },
    cell: {
        marginRight: 16,
    },
    columns: {
        flexDirection: 'column',
        marginBottom: 8,
    }
});

type TableProp = {
    data: DataFrame
}

const Table: React.FC<TableProp> = ({ data }) => {
    return (
        <View style={styles.container}>
             {
                data.columns.map((col: string, idx: number) =>
                    <View style = {styles.columns} key = {`tableColumn-${idx}}`}>
                        <Text style={styles.headerText} key={`tableHeader-${idx}`}>{col}</Text>
                        {
                            data.column(col).values.map((item:any, jdx: number) => (
                                <Text style= {styles.cell} key = {`tableHeader-${idx}-${jdx}`}>{item}</Text>
                            ))
                        }
                    </View>
                    )
            }
        </View>
    )
};

export default Table;