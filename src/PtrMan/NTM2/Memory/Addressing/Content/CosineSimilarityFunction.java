package NTM2.Memory.Addressing.Content;

import NTM2.Controller.Unit;

public class CosineSimilarityFunction implements ISimilarityFunction
{
    private double _uv = 0.0;
    private double _normalizedU = 0.0;
    private double _normalizedV = 0.0;

    @Override
    public Unit calculate(Unit[] u, Unit[] v) {
        for (int i = 0;i < u.length;i++) {
            _uv += u[i].value * v[i].value;
            _normalizedU += u[i].value * u[i].value;
            _normalizedV += v[i].value * v[i].value;
        }
        _normalizedU = Math.sqrt(_normalizedU);
        _normalizedV = Math.sqrt(_normalizedV);
        Unit data = new Unit(_uv / (_normalizedU * _normalizedV));
        if (Double.isNaN(data.value)) {
            throw new RuntimeException("Cosine similarity is nan -> error");
        }
         
        return data;
    }

    @Override
    public void differentiate(Unit similarity, Unit[] uVector, Unit[] vVector) {
        double uvuu = _uv / (_normalizedU * _normalizedU);
        double uvvv = _uv / (_normalizedV * _normalizedV);
        double uvg = similarity.grad / (_normalizedU * _normalizedV);
        for (int i = 0;i < uVector.length;i++) {
            double u = uVector[i].value;
            double v = vVector[i].value;
            uVector[i].grad += (v - (u * uvuu)) * uvg;
            vVector[i].grad += (u - (v * uvvv)) * uvg;
        }
    }

}


