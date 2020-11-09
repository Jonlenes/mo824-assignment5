package problems.qbft;

import java.io.IOException;

public class QBFT_Inverse  extends QBFT {

	public QBFT_Inverse(String filename) throws IOException {
		super(filename);
	}

	@Override
	public Double evaluateQBFT() {
		return -super.evaluateQBFT();
	}
	
	@Override
	public Double evaluateInsertionQBFT(int i) {	
		return -super.evaluateInsertionQBFT(i);
	}
	
	@Override
	public Double evaluateRemovalQBFT(int i) {
		return -super.evaluateRemovalQBFT(i);
	}
	
	@Override
	public Double evaluateExchangeQBFT(int in, int out) {
		return -super.evaluateExchangeQBFT(in,out);
	}

}
