import com.wayeal.cloud.utils.silt_height_calculation.CurveType;
import com.wayeal.cloud.utils.silt_height_calculation.Pipeline;
import com.wayeal.cloud.utils.silt_height_calculation.SiltJudgment;
import com.wayeal.cloud.utils.silt_height_calculation.SiltState;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;

public class SiltHeightCalculationTest {
    @Test
    public void siltHeightCalculationTest() {
        SiltJudgment siltJudgment = new SiltJudgment();
        Pipeline pipeline = new Pipeline(1, 0.015, 0.003, 0.01);
        Pair<SiltState, Double> result = siltJudgment.judge(pipeline, 2.5455, 5.3543, 0.7);
        System.out.println(result.getValue());
    }

    @Test
    public void siltHeightCalculationTest2() {
        SiltJudgment siltJudgment = new SiltJudgment();
        Pipeline pipeline = new Pipeline(1, 0.015, 0.003, 0.01);
        Pair<SiltState, Double> result = siltJudgment.judge(pipeline, CurveType.Q, 2.5455, 0.7);
        System.out.println(result.getValue());
        Pair<SiltState, Double> result1 = siltJudgment.judge(pipeline, CurveType.V, 5.3543, 0.7);
        System.out.println(result1.getValue());

    }
}
