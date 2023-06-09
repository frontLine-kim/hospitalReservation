package com.example.hospitalreservation.hospital.service.impl;

import com.example.hospitalreservation.hospital.dto.HospitalDto;
import com.example.hospitalreservation.hospital.repository.HospitalRepository;
import com.example.hospitalreservation.hospital.service.HospitalService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@RequiredArgsConstructor
@Transactional
@Service
public class HospitalServiceImpl implements HospitalService {

    static String key = "dyhaTKfd3bzVETVOLeeAakllPgL04jJOA5PHlSd3PuesveRRCbfJ0hWY6WrLyxSfEIorAmeb9Gl6yj56%2BeI1yg%3D%3D";
    private final HospitalRepository hospitalRepository;

    // tag값의 정보를 가져오는 함수
//    public static String getTagValue(String tag, Element eElement) {
//
//        //결과를 저장할 result 변수 선언
//        String result = "";
//
//        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
//
//        result = nlList.item(0).getTextContent();
//
//        return result;
//    }

    // tag값의 정보를 가져오는 함수
    public static String getTagValue(String tag, String childTag, Element eElement) {

        //결과를 저장할 result 변수 선언
        String result = "";

        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();

        for (int i = 0; i < eElement.getElementsByTagName(childTag).getLength(); i++) {

            //result += nlList.item(i).getFirstChild().getTextContent() + " ";
            result += nlList.item(i).getChildNodes().item(0).getTextContent() + " ";
        }

        return result;
    }

    private static String getTagValue(String tag, Element eElement) {
        Node nValue=null;

        NodeList x= eElement.getElementsByTagName(tag);
        Node test=x.item(0);
        NodeList t=null;
        if(test!=null) {
            t= test.getChildNodes();
            if((Node)t.item(0)!=null) {nValue=(Node)t.item(0);}
        }
        if(nValue==null) return null;
        return nValue.getNodeValue();
    }


    @Override
    public void getHospitalList() {
        int page = 1;
        try {
            while(true) {
                // parsing할 url 지정(API 키 포함해서)
                String url = "https://apis.data.go.kr/B551182/hospInfoServicev2/getHospBasisList?serviceKey=" + key + "&pageNo=" + page + "&numOfRows=1000";

                DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
                Document doc = dBuilder.parse(url);

                // 제일 첫번째 태그
                doc.getDocumentElement().normalize();

                // 파싱할 tag
                NodeList nList = doc.getElementsByTagName("item");

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;
                        String yadmNm = getTagValue("yadmNm", eElement);
                        String addr = getTagValue("addr", eElement);
                        String telno = getTagValue("telno", eElement);
                        String dgsbjtCd = getTagValue("dgsbjtCd", eElement);
                        HospitalDto hospitalDto = new HospitalDto(null, yadmNm, addr, telno, dgsbjtCd);
                        hospitalRepository.save(hospitalDto.toEntity());

                        
                    } //for
                }
                page += 1;
                if(page>2)  //nList.getLength() 시 모든 페이지 호출
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public HospitalDto getHospital(Long hospitalId){
        return hospitalRepository.findById(hospitalId)
                .map(HospitalDto::from)
                .orElseThrow(() -> new EntityNotFoundException("병원이 없습니다"));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<HospitalDto> searchHospitalList(String searchKeyword, Pageable pageable){
        if(searchKeyword == null || searchKeyword.isBlank()){
            return hospitalRepository.findAll(pageable).map(HospitalDto::from);
        }

        return hospitalRepository.findByYadmNmContaining(searchKeyword, pageable).map(HospitalDto::from);
    }

}
