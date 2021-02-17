class TestWeerOnlineHubitatDriver extends GroovyTestCase {

    void testDriver() {
        def binding = new Binding()
        def shell = new GroovyShell(binding)
        binding.setVariable('x',1)
        binding.setVariable('y',3)
        binding.setVariable('metadata', { })
        //binding.setVariable('getThisCopyright', { })


        shell.evaluate 'z=2*x+y'
        def anObject = shell.parse(new File("src/main/groovy/nu/leeuwen/hubitat/WeerOnlineHubitatDriver.groovy"))
        System.print anObject.getThisCopyright()
        anObject.metaClass.test = {-> "aaaargh"}
        assertEquals(anObject.getThisCopyright(),"a")
        assert binding.getVariable('z') == 5
    }

}
