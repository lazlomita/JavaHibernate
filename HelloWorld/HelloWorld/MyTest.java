

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class MyTest.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */
public class MyTest
{
    /**
     * Default constructor for test class MyTest
     */
    public MyTest()
    {
        Greeter myGreeter = new Greeter();
        System.out.println(myGreeter.sampleMethod(25));
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp()
    {
        Greeter myGreeter = new Greeter();
        System.out.println(myGreeter.sampleMethod(35));
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @After
    public void tearDown()
    {
        Greeter myGreeter = new Greeter();
        System.out.println(myGreeter.sampleMethod(45));
    }
}
