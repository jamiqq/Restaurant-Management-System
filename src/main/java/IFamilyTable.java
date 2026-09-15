public interface IFamilyTable {

    boolean getIsExtendable();
    void setIsExtendable(boolean val);
    boolean getHasInfantChair();
    void setHasInfantChair(boolean val);
    double calculateKidDiscount(int numOfKids);
}
