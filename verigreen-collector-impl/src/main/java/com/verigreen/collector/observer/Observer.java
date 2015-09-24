package com.verigreen.collector.observer;

import com.verigreen.collector.api.VerificationStatus;


public interface Observer
{
	public void update(VerificationStatus status);
}
