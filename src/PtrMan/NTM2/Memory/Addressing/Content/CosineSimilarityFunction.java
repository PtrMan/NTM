package NTM2.Memory.Addressing.Content;

import NTM2.Controller.Unit;

public class CosineSimilarityFunction implements ISimilarityFunction
{
    private double _uv;
    private double _normalizedU;
    private double _normalizedV;
    public Unit calculate(Unit[] u, Unit[] v) {
        for (int i = 0;i < u.length;i++) {
            _uv += u[i].Value * v[i].Value;
            _normalizedU += u[i].Value * u[i].Value;
            _normalizedV += v[i].Value * v[i].Value;
        }
        _normalizedU = Math.sqrt(_normalizedU);
        _normalizedV = Math.sqrt(_normalizedV);
        Unit data = new Unit(_uv / (_normalizedU * _normalizedV));
        if (Double.isNaN(data.Value)) {
            throw new RuntimeException("Cosine similarity is nan -> error");
        }
         
        return data;
    }

    public void differentiate(Unit similarity, Unit[] uVector, Unit[] vVector) {
        double uvuu = _uv / (_normalizedU * _normalizedU);
        double uvvv = _uv / (_normalizedV * _normalizedV);
        double uvg = similarity.Gradient / (_normalizedU * _normalizedV);
        for (int i = 0;i < uVector.length;i++) {
            double u = uVector[i].Value;
            double v = vVector[i].Value;
            uVector[i].Gradient += (v - (u * uvuu)) * uvg;
            vVector[i].Gradient += (u - (v * uvvv)) * uvg;
        }
    }

}


