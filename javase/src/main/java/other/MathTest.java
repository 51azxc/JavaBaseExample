package other;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * 数学相关知识
 */
public class MathTest {

	/*
	 * 保留2位小数
	 */
	public void towDecimal() {
		double d = 12345.6789;
		BigDecimal bg = new BigDecimal(d);
		double format_d =  bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		System.out.println("BigDecimal舍入方法: "+format_d);

		DecimalFormat df = new DecimalFormat("#.00");
		System.out.println("DecimalFormat转换方法: "+df.format(d));

		System.out.println("使用String.format方法: "+String.format("%.2f", d));

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		System.out.println("NumberFormat方法: "+nf.format(d));
	}
	
	/*
	 * BigDecimal相关用法
	 * BigDecimal舍入模式主要有下
     * ROUND_CEILING: 向正无穷方向舍入
     * ROUND_DOWN: 向零方向舍入
     * ROUND_FLOOR: 向负无穷方向舍入
     * ROUND_HALF_DOWN: 向（距离）最近的一边舍入，
     *   除非两边（的距离）是相等,如果是这样，向下舍入, 例如1.55 保留一位小数结果为1.5
     * ROUND_HALF_EVEN: 向（距离）最近的一边舍入，
     *   除非两边（的距离）是相等,如果是这样，向上舍入, 1.55保留一位小数结果为1.6
     * ROUND_UNNECESSARY: 计算结果是精确的，不需要舍入模式
     * ROUND_UP: 向远离0的方向舍入
	 */
	public void useBigDecimal() {
		double d1 = 12345.6789;
		double d2 = 98765.4321;
		BigDecimal bg1 = new BigDecimal(d1);
		BigDecimal bg2 = new BigDecimal(d2);
		System.out.println("加: "+bg1.add(bg2).toString());
		System.out.println("减: "+bg1.subtract(bg2).doubleValue());
		System.out.println("乘: "+bg1.multiply(bg2).floatValue());
		//防止出现无限小数设置小数位
		System.out.println("除:"+bg1.divide(bg2,2,BigDecimal.ROUND_HALF_EVEN).intValue());
	}
}
