package ntm.memory.address.content;

import ntm.control.Unit;

public class CosineSimilarityFunction implements ISimilarityFunction
{

    double _uv;
    double _normalizedU, _normalizedV;

    @Override
    public Unit calculate(Unit[] u, Unit[] v) {
        _uv = 0;
        _normalizedU = _normalizedV = 0;

        for (int i = 0;i < u.length;i++) {
            final double uV = u[i].value;
            final double vV = v[i].value;
            _uv += uV * vV;
            _normalizedU += uV * uV;
            _normalizedV += vV * vV;
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


