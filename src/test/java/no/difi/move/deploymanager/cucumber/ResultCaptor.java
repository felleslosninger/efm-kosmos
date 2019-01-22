package no.difi.move.deploymanager.cucumber;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ResultCaptor<T> implements Answer<T> {
    private final Class<T> classType;

    @Getter
    private List<T> values = new ArrayList<>();

    T getLastValue() {
        return values.get(values.size() - 1);
    }

    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        T result = classType.cast(invocationOnMock.callRealMethod());
        values.add(result);
        return result;
    }

    void reset() {
        values.clear();
    }
}
