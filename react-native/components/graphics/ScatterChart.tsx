import * as d3 from "d3";
import React from "react";
import { useWindowDimensions } from "react-native";
import { Circle, Svg, Text } from "react-native-svg";


type ScatterProp = {
    xvals: number[],
    yvals: number[],
    xlabel: string,
    ylabel: string,
    title: string,
}

const ScatterChart: React.FC<ScatterProp> = ({ xvals, yvals, xlabel, ylabel, title }) => {
    let  {height, width} = useWindowDimensions();
    height /= 3;
    const margin = 10;
    
    const [th, yw, xh] = [40, 35, 35];
    // Total
    const [x,y,h,w] = [margin, margin, height- 2*margin, width - 2*margin];
    // Title
    const [tx, ty, _1, tw] = [x, y, undefined, w];
    // Yaxis
    const [yx, yy, yh, _2] = [x, ty + th, h+y-xh-ty-th, undefined];
    // Chart
    const [cx, cy, ch, cw] = [yx+yw, ty+th, h+y-xh-ty-th, w-yw-yx];
    // Xaxis
    const [xx, xy, _3, xw] = [yx+yw, y + h - xh, undefined, w-yw];
    
    // x axis 설정
    const xscale_width = (d3.max(xvals) as number- (d3.min(xvals) as number))
    const xscale = d3.scaleLinear()
        .domain([d3.min(xvals) as number - .2*xscale_width, d3.max(xvals) as number + .15*xscale_width])
        .range([cx, cx+cw]);
    // y axis 설정
    const ytick_padding = 2;
    const yscale = d3.scaleLinear()
        .domain([0, d3.max(yvals) as number])
        .range([0, ch]);
    const yscale_width = d3.max(yvals) as number
    const scale = Math.round(yscale_width /40) * 10
    const yticks = d3.range(0, d3.max(yvals) as number + scale, scale);
    
    let path = ""
    xvals.map((val, idx) => {
        path = path.concat(idx === 0? "M ":"L ")
        path = path.concat(`${xscale(xvals[idx])} ${cy + ch - yscale(yvals[idx])} `)
    })
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
                    key = "xlabel"
                    x = {xx + xw/2}
                    y = {xy + xh}
                    alignmentBaseline="text-bottom"
                    textAnchor="middle"
                    fontSize= {14}
                >{xlabel}</Text>
                <Text 
                    textAnchor="middle"
                    x = {yx}
                    y = {cy + ch/2}
                    alignmentBaseline = "text-top"
                    fontSize= {14}
                    transform={`rotate(-90, ${yx}, ${cy + ch/2})`}
                >{ylabel}</Text>
                {yvals.map((val, idx) => (
                    <Circle
                        key = {`dot-${idx}`}
                        x = {xscale(xvals[idx])}
                        y = {cy + (ch - yscale(val))}
                        r = {2}
                        fill = {colors[0]}
                    />
                ))}
                {xvals.map((val, idx) => (
                    <Text
                        key={`xtick-${idx}`}
                        x = {xscale(val)}
                        y = {xy}
                        alignmentBaseline="top"
                        textAnchor="middle"   
                    >
                        {val}
                    </Text>
                ))}
                {yticks.map((val: number, idx) =>(
                    <Text
                        key={`ytick-${idx}`}
                        x = {cx - ytick_padding}
                        y = {yy + yh - yscale(val)}
                        textAnchor="end"
                        alignmentBaseline="central"
                    >
                        {val}
                    </Text>
                ))}
            </Svg>      
    );
};

export default ScatterChart;