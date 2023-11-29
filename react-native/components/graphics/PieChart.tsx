import * as d3 from "d3";
import React from "react";
import { useWindowDimensions } from "react-native";
import { Circle, Rect, Svg, Text } from "react-native-svg";


type PieProp = {
    xvals: string[],
    yvals: number[],
    xlabel: string,
    ylabel: string,
    title: string,
}

const PieChart: React.FC<PieProp> = ({ xvals, yvals, xlabel, ylabel, title }) => {
    let  {height, width} = useWindowDimensions();
    height /= 3;
    const margin = 10;
    
    const [th, yw, xh] = [40, 25, 35];
    // Total
    const [x,y,h,w] = [margin, margin, height- 2*margin, width - 2*margin];
    // Title
    const [tx, ty, _1, tw] = [x, y, undefined, w];
    // Chart
    let [cx, cy, ch, cw] = [x, y+ th, h-th, w];
    // PieChart
    const outer = Math.min(cw/2, ch/2);
    const inner = outer * .4;
    const [r, stroke_width] = [(outer+inner)/2, (outer-inner)];
    const rx = cx+ outer
    const ry = cy+ outer
    // Legend
    const [lx, ly, lh ,lw] = [cx + 2*outer, cy, ch, cw - 2*outer];
    const lsize = 14;

    const colors = ["#7A80F7","#71F8D6", "#FAD375", "#EE7D78"]
    
    return (
            <Svg width={width} height={height}>
                <Text
                    key={"title"}
                    x = {tx}
                    y = {ty}
                    alignmentBaseline="text-top"
                    textAnchor="start"
                    fontSize = {16}
                >
                    {title}
                </Text>
                <Text
                    key={"xlabel"}
                    x = {rx}
                    y = {ry}
                    alignmentBaseline="central"
                    textAnchor="middle"
                    fontSize = {16}
                >
                    {xlabel}
                </Text>
                {
                    d3.range(yvals.length-1, -1, -1).map((idx) => (
                     <Circle
                        key={`pie-${idx}`}
                        r={r} cx = {rx} cy = {ry}
                        stroke = {colors[idx]}
                        fill = {"transparent"}
                        strokeWidth = {stroke_width}
                        transform = {`rotate(-90, ${rx}, ${ry})`}
                        // transform = {`rotate(-90 + ${-90 + (idx === 0? 0: 20*idx)},${rx}, ${ry})`}
                        strokeDasharray={`${(d3.cumsum(yvals)[idx] / d3.sum(yvals)) * Math.PI * r*2} ${Math.PI * r*2}`}
                     />   
                    ))
                }
                {
                    d3.range(yvals.length-1, -1, -1).map((idx) => (
                     <Rect
                        key={`legend-${idx}`}
                        x = {lx + 10}
                        y = {ly + idx * 30}
                        height= {lsize}
                        width = {lsize}
                        fill = {colors[idx]}
                     />   
                    ))
                }
                {
                    d3.range(yvals.length-1, -1, -1).map((idx) => (
                     <Text
                        key={`legend-category-${idx}`}
                        x = {lx + 10 + lsize + 10}
                        y = {ly + idx * 30}
                        alignmentBaseline="text-top"
                        textAnchor="start"
                     >
                        {`${xvals[idx]}, ${Math.round(yvals[idx]/ d3.sum(yvals) * 1000)/10}%`}
                     </Text>   
                    ))
                }
                
            </Svg>      
    );
};

export default PieChart;